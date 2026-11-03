import defaults from "../fixtures/system-settings.json";

type Settings = typeof defaults;

const state = globalThis as typeof globalThis & {
  consoleSystemSettings?: Settings;
};

function settingsState(): Settings {
  if (!state.consoleSystemSettings) {
    state.consoleSystemSettings = { ...defaults };
  }
  return state.consoleSystemSettings;
}

export async function GET(): Promise<Response> {
  return Response.json(settingsState(), {
    headers: { "cache-control": "no-store" },
  });
}

export async function PATCH(request: Request): Promise<Response> {
  let input: { key?: unknown; value?: unknown };
  try {
    input = (await request.json()) as { key?: unknown; value?: unknown };
  } catch {
    return Response.json({ error: "请求格式无效" }, { status: 400 });
  }
  if (
    typeof input.key !== "string" ||
    !Object.hasOwn(defaults, input.key) ||
    typeof input.value !== "string"
  ) {
    return Response.json({ error: "设置项无效" }, { status: 400 });
  }
  const key = input.key as keyof Settings;
  const value = input.value.trim();
  const valid =
      (key === "siteName" && value.length > 0 && value.length <= 80) ||
      (key === "description" && value.length <= 200) ||
      (key === "timezone" && ["Asia/Shanghai", "UTC", "America/New_York"].includes(value)) ||
      (key === "language" && ["zh-CN", "en-US"].includes(value)) ||
      (key === "pageSize" && ["10", "20", "50"].includes(value)) ||
      (key === "sessionTimeout" && ["15", "30", "60", "120"].includes(value));
  if (!valid) {
    return Response.json({ error: "设置值无效" }, { status: 400 });
  }
  settingsState()[key] = value;
  return Response.json({ key, value });
}
