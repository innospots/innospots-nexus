import { portalRegister } from "@/backend/api/auth";
import { corsPreflight } from "@/backend/cors";

export function POST(request: Request) {
  return portalRegister(request);
}

export const OPTIONS = corsPreflight;
