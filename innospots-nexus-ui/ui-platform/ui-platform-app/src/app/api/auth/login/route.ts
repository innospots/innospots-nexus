import { corsHeaders, deprecatedDemoLogin } from "@/backend/api";

/** 早期演示登录（mock 账号）已停用；请使用 POST /api/auth/platform-login。 */
export async function POST(request: Request) {
  return deprecatedDemoLogin(request);
}

/** 预检（跨源 POST JSON） */
export function OPTIONS(request: Request): Response {
  return new Response(null, { status: 204, headers: corsHeaders(request) });
}
