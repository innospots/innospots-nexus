import { corsHeaders, platformLogout } from "@/backend/api";

/** 登出：清理平台 access token 与历史演示会话 cookie */
export async function POST(request: Request) {
  return platformLogout(request);
}

/** 预检（跨源 POST JSON） */
export function OPTIONS(request: Request): Response {
  return new Response(null, { status: 204, headers: corsHeaders(request) });
}
