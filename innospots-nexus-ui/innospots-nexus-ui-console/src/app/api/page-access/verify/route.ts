import { cookies } from "next/headers";

import {
  readPlatformAccessToken,
  resolveSessionUser,
} from "@/backend/auth/accounts";
import { corsHeaders, corsPreflight } from "@/backend/cors";
import { verifyPageAccess } from "@/backend/pages/page-access";

interface VerifyBody {
  path?: string;
  pageId?: string;
}

export async function POST(request: Request) {
  const body = (await request.json().catch(() => ({}))) as VerifyBody;
  const pathname =
    typeof body.path === "string" && body.path !== "" ? body.path : undefined;
  const pageId =
    typeof body.pageId === "string" && body.pageId !== "" ? body.pageId : undefined;

  if (pathname === undefined && pageId === undefined) {
    return Response.json(
      { allowed: false, reason: "缺少 path 或 pageId" },
      { status: 400, headers: corsHeaders(request) },
    );
  }

  const store = await cookies();
  const user = resolveSessionUser(store);
  try {
    const result = await verifyPageAccess({
      pathname,
      pageId,
      user,
      accessToken: readPlatformAccessToken(store),
      cookie: request.headers.get("cookie"),
    });

    return Response.json(result, {
      headers: { "cache-control": "no-store", ...corsHeaders(request) },
    });
  } catch (error) {
    const message =
      error instanceof Error ? error.message : "页面访问校验失败";
    return Response.json(
      { allowed: false, reason: message },
      {
        status: 200,
        headers: { "cache-control": "no-store", ...corsHeaders(request) },
      },
    );
  }
}

export const OPTIONS = corsPreflight;
