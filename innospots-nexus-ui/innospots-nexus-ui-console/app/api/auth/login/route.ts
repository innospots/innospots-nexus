import { NextResponse } from "next/server";

import { corsHeaders, corsPreflight } from "@/server/cors";

/**
 * 早期演示登录（mock 账号）已停用；请使用 {@code POST /api/auth/platform-login}。
 */
export async function POST(request: Request) {
  return NextResponse.json(
    {
      code: "DEPRECATED",
      message: "演示登录已停用，请使用平台登录接口 /api/auth/platform-login",
    },
    { status: 410, headers: corsHeaders(request) },
  );
}

/** 预检（跨源 POST JSON） */
export const OPTIONS = corsPreflight;
