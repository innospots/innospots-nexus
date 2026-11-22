import { NextResponse } from "next/server";

import {
  ACCESS_TOKEN_COOKIE,
  PLATFORM_LOGIN_COOKIE,
  SESSION_COOKIE,
} from "@/backend/session";
import { corsHeaders, corsPreflight } from "@/backend/cors";

/** 登出：清平台会话与演示会话 cookie */
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
  for (const name of [
    SESSION_COOKIE,
    ACCESS_TOKEN_COOKIE,
    PLATFORM_LOGIN_COOKIE,
  ]) {
    response.cookies.set(name, "", clear);
  }
  return response;
}

/** 预检（跨源 POST JSON） */
export const OPTIONS = corsPreflight;
