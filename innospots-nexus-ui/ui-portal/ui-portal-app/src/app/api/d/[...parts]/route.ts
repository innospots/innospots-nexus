import { forwardDataRequest } from "@/backend/api/data-proxy";
import { corsPreflight } from "@/backend/cors";

type Context = { params: Promise<{ parts?: string[] }> };

async function handle(request: Request, context: Context) {
  const { parts = [] } = await context.params;
  return forwardDataRequest(request, parts);
}

export const GET = handle;
export const POST = handle;
export const PUT = handle;
export const PATCH = handle;
export const DELETE = handle;
export const OPTIONS = corsPreflight;
