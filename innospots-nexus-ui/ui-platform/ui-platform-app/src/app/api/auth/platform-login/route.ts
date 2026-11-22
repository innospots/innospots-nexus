import { corsHeaders, platformLogin } from "@/backend/api";

/** 代理平台登录并写入 httpOnly 会话 cookie（实现见 src/backend/api/auth.ts） */
export async function POST(request: Request) {
  return platformLogin(request);
}

/** 预检（跨源 POST JSON） */
export function OPTIONS(request: Request): Response {
  return new Response(null, { status: 204, headers: corsHeaders(request) });
}
