import { portalSession } from "@/backend/api/auth";
import { corsPreflight } from "@/backend/cors";

export function GET(request: Request) {
  return portalSession(request);
}

export const OPTIONS = corsPreflight;
