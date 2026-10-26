import type { PageDsl } from "@pactor-app/core";

type MessageTable = Readonly<Record<string, string>>;
type PathSegment = string | number;

function pathToKey(namespace: string, pageId: string, path: PathSegment[]): string {
  const normalized = path[0] === "page" && (path[1] === "title" || path[1] === "description")
    ? ["body", path[1]]
    : path;
  return `${namespace}.${pageId}.${normalized.map(String).join(".")}`;
}

function localizedValue(
  value: unknown,
  path: PathSegment[],
  namespace: string,
  pageId: string,
  messages: MessageTable,
): unknown {
  if (typeof value === "string") {
    return messages[pathToKey(namespace, pageId, path)] ?? value;
  }
  if (Array.isArray(value)) {
    return value.map((item, index) =>
      localizedValue(item, [...path, index], namespace, pageId, messages));
  }
  if (!value || typeof value !== "object") {
    return value;
  }
  return Object.fromEntries(
    Object.entries(value)
      .filter(([key]) => key !== "i18n")
      .map(([key, child]) => [
        key,
        localizedValue(child, [...path, key], namespace, pageId, messages),
      ]),
  );
}

/**
 * 将作者态页面 DSL 转换为服务端本地化结果；不修改缓存中的原始对象。
 */
export function localizePageDsl(
  page: PageDsl,
  pageId: string,
  namespace: string,
  messages: MessageTable,
): PageDsl {
  return localizedValue(page, [], namespace, pageId, messages) as PageDsl;
}

export function localizeSitemapValue(
  value: unknown,
  path: PathSegment[],
  messages: MessageTable,
): unknown {
  if (typeof value === "string") {
    return messages[`common.sitemap.${path.map(String).join(".")}`] ?? value;
  }
  if (Array.isArray(value)) {
    return value.map((item, index) =>
      localizeSitemapValue(item, [...path, index], messages));
  }
  if (!value || typeof value !== "object") {
    return value;
  }
  return Object.fromEntries(
    Object.entries(value).map(([key, child]) => [
      key,
      localizeSitemapValue(child, [...path, key], messages),
    ]),
  );
}
