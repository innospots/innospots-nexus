import { NextResponse } from "next/server";

import {
  ACCESS_TOKEN_COOKIE,
  LEGACY_DEMO_SESSION_COOKIE,
  PLATFORM_LOGIN_COOKIE,
} from "@/server/accounts";
import { corsHeaders, corsPreflight } from "@/server/cors";

/** 演示登出：清会话 cookie */
export async function POST(request: Request) {
  const response = NextResponse.json(
    { ok: true },
    { headers: corsHeaders(request) },
  );
  const clear = {
    httpOnly: true,
    sameSite: "lax" as const,
    path: "/",
    maxAge: 0,
  };
  response.cookies.set(LEGACY_DEMO_SESSION_COOKIE, "", clear);
  response.cookies.set(ACCESS_TOKEN_COOKIE, "", clear);
  response.cookies.set(PLATFORM_LOGIN_COOKIE, "", clear);
  return response;
}

/** 预检（跨源 POST JSON） */
export const OPTIONS = corsPreflight;
