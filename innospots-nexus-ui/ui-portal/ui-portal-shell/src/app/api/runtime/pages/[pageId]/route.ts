import { cookies } from "next/headers";

import { createRuntimeHandlers } from "@pactor-app/shell/server/routes";

import { resolveSessionUser } from "@/backend/session";

const handlers = createRuntimeHandlers({
  cookies,
  resolveUser: (store) => resolveSessionUser(store),
});

export const GET = handlers.page;
