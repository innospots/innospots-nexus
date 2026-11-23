import { portalLogin } from "@/backend/api/auth";
import { corsPreflight } from "@/backend/cors";

export function POST(request: Request) {
  return portalLogin(request);
}

export const OPTIONS = corsPreflight;
