import type { PageDsl } from "@pactor-app/core";

type RequestCheck = { method: string; url: string };
type ObjectValue = Record<string, unknown>;
type AccessResult = { allowed: Record<string, boolean>; grantGroups: string[] };

function object(value: unknown): value is ObjectValue {
  return typeof value === "object" && value !== null && !Array.isArray(value);
}

function requestKey(request: RequestCheck): string {
  return `${request.method}:${request.url}`;
}

/** 公开页面不进行授权检查，仅移除服务端使用的组件依赖声明。 */
export function stripAccessDeclarations(value: unknown): unknown {
  if (Array.isArray(value)) return value.map(stripAccessDeclarations);
  if (!object(value)) return value;
  return Object.fromEntries(Object.entries(value)
    .filter(([key]) => key !== "access")
    .map(([key, child]) => [key, stripAccessDeclarations(child)]));
}

/** 收集事件链、嵌入式弹层及显式声明内的 HTTP 请求。 */
export function collectActionRequests(value: unknown, namedActions: ObjectValue = {}): RequestCheck[] {
  const requests = new Map<string, RequestCheck>();
  const visited = new Set<string>();
  const walk = (node: unknown) => {
    if (Array.isArray(node)) {
      node.forEach(walk);
      return;
    }
    if (!object(node)) return;
    if (node.action === "call") {
      const params = object(node.params) ? node.params : {};
      const name = typeof params.name === "string" ? params.name : "";
      if (!name || !Object.hasOwn(namedActions, name)) {
        requests.set(":", { method: "", url: "" });
      } else if (!visited.has(name)) {
        visited.add(name);
        walk(namedActions[name]);
      }
    }
    if (node.action === "request") {
      const params = object(node.params) ? node.params : {};
      const method = typeof params.method === "string" ? params.method.toUpperCase() : "GET";
      const url = typeof params.url === "string" ? params.url : "";
      const request = { method, url };
      requests.set(requestKey(request), request);
    }
    if (object(node.access) && node.access.requests !== undefined) {
      if (!Array.isArray(node.access.requests)) {
        requests.set(":", { method: "", url: "" });
      } else {
        for (const item of node.access.requests) {
          const request = {
            method: object(item) && typeof item.method === "string" ? item.method.toUpperCase() : "",
            url: object(item) && typeof item.url === "string" ? item.url : "",
          };
          requests.set(requestKey(request), request);
        }
      }
    }
    Object.values(node).forEach(walk);
  };
  walk(value);
  return [...requests.values()];
}

/** 用服务端授权结果移除按钮或禁止事件；原页面数据不被修改。 */
export function applyActionAccess(
  page: PageDsl, result: AccessResult, failed: boolean, failureMessage = "权限检查失败，相关操作不可用",
): PageDsl {
  const groups = new Set(result.grantGroups);
  const namedActions = object(page.actions) ? page.actions : {};
  const omitted = Symbol("unauthorized component");
  const deniedGroups = (value: unknown): boolean => {
    const visited = new Set<string>();
    const walk = (node: unknown): boolean => {
      if (Array.isArray(node)) return node.some(walk);
      if (!object(node)) return false;
      if (node.action === "call") {
        const params = object(node.params) ? node.params : {};
        const name = typeof params.name === "string" ? params.name : "";
        if (!name || !Object.hasOwn(namedActions, name)) return true;
        if (!visited.has(name)) {
          visited.add(name);
          if (walk(namedActions[name])) return true;
        }
      }
      if (node.access !== undefined && !object(node.access)) return true;
      if (object(node.access) && node.access.grantGroups !== undefined) {
        if (!Array.isArray(node.access.grantGroups) || node.access.grantGroups.some((group) =>
          typeof group !== "string" || (!groups.has("*") && !groups.has(group)))) return true;
      }
      return Object.values(node).some(walk);
    };
    return walk(value);
  };
  const denied = (value: unknown) => deniedGroups(value) || collectActionRequests(value, namedActions)
    .some((request) => result.allowed[requestKey(request)] !== true);
  const transform = (value: unknown): unknown => {
    if (Array.isArray(value)) return value.map(transform).filter((item) => item !== omitted);
    if (!object(value)) return value;
    const isComponent = typeof value.type === "string";
    const access = object(value.access) ? value.access : {};
    const requiredGroups = Array.isArray(access.grantGroups) ? access.grantGroups : [];
    const invalidAccess = value.access !== undefined && !object(value.access);
    const invalidGroups = access.grantGroups !== undefined && !Array.isArray(access.grantGroups);
    const deniedGroup = invalidAccess || invalidGroups || requiredGroups.some((group) =>
      typeof group !== "string" || (!groups.has("*") && !groups.has(group)));
    const deniedOwnRequests = denied({ access: value.access });
    const events = object(value.events) ? value.events : {};
    const deniedEvent = Object.values(events).some(denied);
    if (isComponent && (deniedGroup || deniedOwnRequests || deniedEvent) && value.type === "Button") {
      return omitted;
    }
    const out: ObjectValue = {};
    for (const [key, child] of Object.entries(value)) {
      if (key === "access") continue;
      if (key === "events" && isComponent && object(child)) {
        const safeEvents: ObjectValue = {};
        for (const [name, actions] of Object.entries(child)) {
          if (!deniedGroup && !deniedOwnRequests && !denied(actions)) {
            safeEvents[name] = transform(actions);
          }
        }
        out[key] = safeEvents;
      } else {
        out[key] = transform(child);
      }
    }
    if (isComponent && (deniedGroup || deniedOwnRequests)) {
      if (value.type !== "Form" && access.mode !== "disable") return omitted;
      out.events = {};
      if (access.mode === "disable") {
        out.props = { ...(object(out.props) ? out.props : {}), disabled: true };
      }
    }
    if (value.type === "Form" && (deniedEvent || deniedOwnRequests || deniedGroup)) {
      const fieldTypes = new Set(["Input", "Textarea", "Select", "Combobox", "NumberInput", "DatePicker",
        "Checkbox", "RadioGroup", "Switch"]);
      const disableFields = (child: unknown): void => {
        if (Array.isArray(child)) return child.forEach(disableFields);
        if (!object(child)) return;
        const isField = typeof child.type === "string" && fieldTypes.has(child.type);
        const isSubmit = child.type === "Button" && object(child.props)
          && (child.props.submit || child.props.htmlType === "submit");
        if (isField || isSubmit) {
          child.props = { ...(object(child.props) ? child.props : {}), disabled: true };
        }
        Object.values(child).forEach(disableFields);
      };
      disableFields(out.children);
    }
    return out;
  };
  const transformed = transform(page) as PageDsl;
  transformed.state = {
    ...transformed.state,
    grantGroupList: result.grantGroups,
    grantGroups: Object.fromEntries(result.grantGroups.map((group) => [group, true])),
    grantGroupsAll: groups.has("*"),
  };
  if (failed && transformed.body) {
    const body = transformed.body as unknown as ObjectValue;
    const alert = { type: "Alert", props: { variant: "warning", message: failureMessage } };
    const children = body.children == null ? [] : Array.isArray(body.children) ? body.children : [body.children];
    body.children = [alert, ...children];
  }
  return transformed;
}

