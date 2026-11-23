import { portalLogout } from "@/backend/api/auth";
import { corsPreflight } from "@/backend/cors";

export function POST(request: Request) {
  return portalLogout(request);
}

export const OPTIONS = corsPreflight;
