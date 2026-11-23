import { portalRsaPublicKey } from "@/backend/api/auth";
import { corsPreflight } from "@/backend/cors";

export function GET(request: Request) {
  return portalRsaPublicKey(request);
}

export const OPTIONS = corsPreflight;
