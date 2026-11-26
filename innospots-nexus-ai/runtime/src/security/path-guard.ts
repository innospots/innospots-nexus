import { lstatSync, realpathSync } from "node:fs";
import { dirname, isAbsolute, relative, resolve, sep } from "node:path";

export class PathGuard {
  private readonly root: string;

  constructor(root: string) {
    this.root = realpathSync(root);
  }

  getRoot(): string {
    return this.root;
  }

  resolve(input: string): string {
    if (!input || input.includes("\0")) throw new Error("path_denied");
    if (isAbsolute(input)) throw new Error("path_outside_root");
    const normalized = input.replaceAll("\\", "/");
    if (normalized.split("/").some((part) => part === "..")) throw new Error("path_outside_root");

    const target = resolve(this.root, normalized);
    const rel = relative(this.root, target);
    if (rel.startsWith("..") || rel.includes(`..${sep}`)) throw new Error("path_outside_root");

    const existing = this.nearestExisting(target);
    this.assertNoSymlink(existing);
    const real = realpathSync(existing);
    const realRel = relative(this.root, real);
    if (realRel.startsWith("..") || realRel.includes(`..${sep}`)) throw new Error("path_outside_root");
    return target;
  }

  assertRegularFile(path: string): void {
    const stat = lstatSync(path);
    if (stat.isSymbolicLink()) throw new Error("symlink_denied");
    if (!stat.isFile()) throw new Error("not_a_file");
  }

  assertDirectory(path: string): void {
    const stat = lstatSync(path);
    if (stat.isSymbolicLink()) throw new Error("symlink_denied");
    if (!stat.isDirectory()) throw new Error("not_a_directory");
  }

  private nearestExisting(path: string): string {
    let current = path;
    while (true) {
      try {
        lstatSync(current);
        return current;
      } catch {
        const parent = dirname(current);
        if (parent === current) throw new Error("path_outside_root");
        current = parent;
      }
    }
  }

  private assertNoSymlink(path: string): void {
    let current = this.root;
    const rel = relative(this.root, path);
    if (!rel) return;
    for (const segment of rel.split(sep)) {
      current = `${current}${sep}${segment}`;
      try {
        if (lstatSync(current).isSymbolicLink()) throw new Error("symlink_denied");
      } catch (error) {
        if (error instanceof Error && error.message === "symlink_denied") throw error;
      }
    }
  }
}
