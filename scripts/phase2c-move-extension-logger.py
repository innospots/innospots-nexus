#!/usr/bin/env python3
"""Move kernel extension/logger packages into console with package rewrites."""

from pathlib import Path

ROOT = Path("/Users/yxy/works/innospots-gen/innospots-nexus")

MOVES = [
    (
        ROOT / "innospots-nexus-kernel/src/main/java/com/innospots/nexus/kernel/extension",
        ROOT / "innospots-nexus-console/src/main/java/com/innospots/nexus/console/extension",
        "com.innospots.nexus.kernel.extension",
        "com.innospots.nexus.console.extension",
    ),
    (
        ROOT / "innospots-nexus-kernel/src/test/java/com/innospots/nexus/kernel/extension",
        ROOT / "innospots-nexus-console/src/test/java/com/innospots/nexus/console/extension",
        "com.innospots.nexus.kernel.extension",
        "com.innospots.nexus.console.extension",
    ),
    (
        ROOT / "innospots-nexus-kernel/src/main/java/com/innospots/nexus/kernel/logger",
        ROOT / "innospots-nexus-console/src/main/java/com/innospots/nexus/console/logger",
        "com.innospots.nexus.kernel.logger",
        "com.innospots.nexus.console.logger",
    ),
]


def rewrite(text: str, old: str, new: str) -> str:
    return text.replace(old, new)


def move_tree(src: Path, dest: Path, old: str, new: str) -> None:
    if not src.exists():
        raise SystemExit(f"missing source: {src}")
    for path in src.rglob("*"):
        if not path.is_file():
            continue
        rel = path.relative_to(src)
        target = dest / rel
        target.parent.mkdir(parents=True, exist_ok=True)
        content = path.read_text(encoding="utf-8")
        target.write_text(rewrite(content, old, new), encoding="utf-8")
        path.unlink()
    # remove emptied directories
    for directory in sorted(src.rglob("*"), reverse=True):
        if directory.is_dir():
            try:
                directory.rmdir()
            except OSError:
                pass
    try:
        src.rmdir()
    except OSError:
        pass


def main() -> None:
    for src, dest, old, new in MOVES:
        move_tree(src, dest, old, new)

    sync = ROOT / (
        "innospots-nexus-kernel/src/main/java/com/innospots/nexus/kernel/"
        "permission/service/PermissionResourceSyncService.java"
    )
    sync.write_text(
        rewrite(
            sync.read_text(encoding="utf-8"),
            "com.innospots.nexus.kernel.extension.service.ExtensionRegistry",
            "com.innospots.nexus.console.extension.service.ExtensionRegistry",
        ),
        encoding="utf-8",
    )
    sync_test = ROOT / (
        "innospots-nexus-kernel/src/test/java/com/innospots/nexus/kernel/"
        "permission/service/PermissionResourceSyncServiceTest.java"
    )
    sync_test.write_text(
        rewrite(
            sync_test.read_text(encoding="utf-8"),
            "com.innospots.nexus.kernel.extension.service.ExtensionRegistry",
            "com.innospots.nexus.console.extension.service.ExtensionRegistry",
        ),
        encoding="utf-8",
    )

    spi_src = ROOT / (
        "innospots-nexus-kernel/src/test/resources/META-INF/services/"
        "com.innospots.nexus.core.extension.contract.ConsoleExtensionProvider"
    )
    spi_dest = ROOT / (
        "innospots-nexus-console/src/test/resources/META-INF/services/"
        "com.innospots.nexus.core.extension.contract.ConsoleExtensionProvider"
    )
    spi_dest.parent.mkdir(parents=True, exist_ok=True)
    spi_dest.write_text(
        rewrite(
            spi_src.read_text(encoding="utf-8"),
            "com.innospots.nexus.kernel.extension",
            "com.innospots.nexus.console.extension",
        ),
        encoding="utf-8",
    )
    spi_src.unlink()
    print("moved extension and logger")


if __name__ == "__main__":
    main()
