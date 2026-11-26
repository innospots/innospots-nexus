#!/usr/bin/env node
import { main } from "../dist/server.mjs";

main().then((code) => {
  if (code !== 0) process.exit(code);
});
