#!/usr/bin/env bash
# Link java:* skills from this repo into Codex CLI and Cursor (repo-local) skill dirs.
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
JAVA_SKILLS_DIR="$REPO_ROOT/skills/java"

CODEX_SKILLS_DIR="${CODEX_SKILLS_DIR:-$HOME/.codex/skills}"
CURSOR_SKILLS_DIR="${CURSOR_SKILLS_DIR:-$REPO_ROOT/.cursor/skills}"

if [[ ! -d "$JAVA_SKILLS_DIR" ]]; then
    echo "error: missing $JAVA_SKILLS_DIR" >&2
    exit 1
fi

mkdir -p "$CODEX_SKILLS_DIR" "$CURSOR_SKILLS_DIR"

linked=0
for skill_dir in "$JAVA_SKILLS_DIR"/java-*; do
    if [[ ! -d "$skill_dir" ]] || [[ ! -f "$skill_dir/SKILL.md" ]]; then
        continue
    fi
    name="$(basename "$skill_dir")"
    ln -sfn "$skill_dir" "$CODEX_SKILLS_DIR/$name"
    ln -sfn "$skill_dir" "$CURSOR_SKILLS_DIR/$name"
    echo "linked $name"
    linked=$((linked + 1))
done

if [[ "$linked" -eq 0 ]]; then
    echo "error: no java-* skills with SKILL.md under $JAVA_SKILLS_DIR" >&2
    exit 1
fi

echo "done: $linked skill(s) -> $CODEX_SKILLS_DIR and $CURSOR_SKILLS_DIR"
