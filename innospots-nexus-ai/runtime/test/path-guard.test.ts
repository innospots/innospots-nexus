import { describe, expect, it } from "vitest";
import { mkdirSync, realpathSync, symlinkSync, writeFileSync } from "node:fs";
import { join } from "node:path";
import { PathGuard } from "../src/security/path-guard.js";
import { tempDir } from "./helpers.js";

describe("path guard", () => {
  it("resolves paths inside root", () => {
    const { dir, cleanup } = tempDir();
    try {
      mkdirSync(join(dir, "sub"));
      writeFileSync(join(dir, "sub/a.txt"), "x");
      const guard = new PathGuard(dir);
      expect(guard.resolve("sub/a.txt")).toBe(join(realpathSync(dir), "sub/a.txt"));
    } finally {
      cleanup();
    }
  });

  it("rejects traversal and absolute paths", () => {
    const { dir, cleanup } = tempDir();
    try {
      const guard = new PathGuard(dir);
      expect(() => guard.resolve("../etc/passwd")).toThrow(/path_outside_root/);
      expect(() => guard.resolve("/etc/passwd")).toThrow(/path_outside_root/);
    } finally {
      cleanup();
    }
  });

  it("rejects symlinks that escape root", () => {
    const { dir, cleanup } = tempDir();
    try {
      const outside = join(dir, "outside");
      mkdirSync(outside);
      symlinkSync(outside, join(dir, "link"));
      const guard = new PathGuard(dir);
      expect(() => guard.resolve("link/file.txt")).toThrow(/symlink_denied|path_outside_root/);
    } finally {
      cleanup();
    }
  });
});
