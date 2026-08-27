#!/usr/bin/env python3
"""Relocate Phase 2b Java types into domain/api/endpoint packages and console IAM."""

from __future__ import annotations

import shutil
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
CONSOLE = ROOT / "innospots-nexus-console"
KERNEL = ROOT / "innospots-nexus-kernel"


def rewrite_text(path: Path, replacements: list[tuple[str, str]]) -> None:
    text = path.read_text(encoding="utf-8")
    original = text
    for old, new in replacements:
        text = text.replace(old, new)
    if text != original:
        path.write_text(text, encoding="utf-8")


def move_java(src: Path, dest: Path, package: str) -> None:
    dest.parent.mkdir(parents=True, exist_ok=True)
    text = src.read_text(encoding="utf-8")
    lines = text.splitlines(keepends=True)
    out: list[str] = []
    replaced = False
    for line in lines:
        if not replaced and line.startswith("package "):
            out.append(f"package {package};\n")
            if line.endswith("\r\n") or (line.endswith("\n") and not line.endswith("\r\n")):
                pass
            replaced = True
        else:
            out.append(line)
    dest.write_text("".join(out), encoding="utf-8")
    src.unlink()


def write_package_info(package: str, comment: str, path: Path) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(f"/**\n * {comment}\n */\npackage {package};\n", encoding="utf-8")


def relocate_auth_and_credential() -> None:
    main = CONSOLE / "src/main/java/com/innospots/nexus/console"
    test = CONSOLE / "src/test/java/com/innospots/nexus/console"

    moves = [
        (main / "auth/PlatformAuthEndpoint.java", main / "auth/endpoint/PlatformAuthEndpoint.java",
         "com.innospots.nexus.console.auth.endpoint"),
        (main / "auth/TenantAuthEndpoint.java", main / "auth/endpoint/TenantAuthEndpoint.java",
         "com.innospots.nexus.console.auth.endpoint"),
        (main / "auth/UserDirectory.java", main / "auth/api/UserDirectory.java",
         "com.innospots.nexus.console.auth.api"),
        (main / "auth/CredentialStore.java", main / "auth/api/CredentialStore.java",
         "com.innospots.nexus.console.auth.api"),
        (main / "auth/MembershipDirectory.java", main / "auth/api/MembershipDirectory.java",
         "com.innospots.nexus.console.auth.api"),
        (main / "auth/AuthUser.java", main / "auth/domain/model/AuthUser.java",
         "com.innospots.nexus.console.auth.domain.model"),
        (main / "auth/CredentialRecord.java", main / "auth/domain/model/CredentialRecord.java",
         "com.innospots.nexus.console.auth.domain.model"),
        (main / "auth/SecurityRealm.java", main / "auth/domain/enums/SecurityRealm.java",
         "com.innospots.nexus.console.auth.domain.enums"),
        (main / "credential/PasswordDecryptor.java", main / "credential/api/PasswordDecryptor.java",
         "com.innospots.nexus.console.credential.api"),
        (main / "credential/PasswordVerificationOperator.java",
         main / "credential/api/PasswordVerificationOperator.java",
         "com.innospots.nexus.console.credential.api"),
        (main / "credential/VerificationType.java",
         main / "credential/domain/enums/VerificationType.java",
         "com.innospots.nexus.console.credential.domain.enums"),
        (test / "auth/AuthEndpointContractsTest.java",
         test / "auth/endpoint/AuthEndpointContractsTest.java",
         "com.innospots.nexus.console.auth.endpoint"),
        (test / "auth/UserDirectoryContractsTest.java",
         test / "auth/api/UserDirectoryContractsTest.java",
         "com.innospots.nexus.console.auth.api"),
        (test / "credential/PasswordDecryptorTest.java",
         test / "credential/api/PasswordDecryptorTest.java",
         "com.innospots.nexus.console.credential.api"),
        (test / "credential/PasswordValidatorTest.java",
         test / "credential/PasswordValidatorTest.java",
         "com.innospots.nexus.console.credential"),
    ]

    for src, dest, package in moves:
        if src.exists():
            move_java(src, dest, package)

    request_dir = main / "auth/request"
    if request_dir.exists():
        for src in request_dir.glob("*.java"):
            move_java(src, main / "auth/domain/request" / src.name,
                      "com.innospots.nexus.console.auth.domain.request")
        shutil.rmtree(request_dir, ignore_errors=True)

    vo_dir = main / "auth/vo"
    if vo_dir.exists():
        for src in vo_dir.glob("*.java"):
            move_java(src, main / "auth/domain/vo" / src.name,
                      "com.innospots.nexus.console.auth.domain.vo")
        shutil.rmtree(vo_dir, ignore_errors=True)

    write_package_info(
        "com.innospots.nexus.console.auth.api",
        "Non-HTTP directory and credential ports implemented by platform and kernel.",
        main / "auth/api/package-info.java")
    write_package_info(
        "com.innospots.nexus.console.auth.endpoint",
        "Jakarta REST authentication contracts for PLATFORM and TENANT realms.",
        main / "auth/endpoint/package-info.java")
    write_package_info(
        "com.innospots.nexus.console.auth.domain.enums",
        "Authentication enumerations.",
        main / "auth/domain/enums/package-info.java")
    write_package_info(
        "com.innospots.nexus.console.auth.domain.model",
        "Internal authentication models that are neither entities nor transport records.",
        main / "auth/domain/model/package-info.java")
    write_package_info(
        "com.innospots.nexus.console.auth.domain.request",
        "Authentication endpoint request records.",
        main / "auth/domain/request/package-info.java")
    write_package_info(
        "com.innospots.nexus.console.auth.domain.vo",
        "Authentication endpoint view records.",
        main / "auth/domain/vo/package-info.java")
    write_package_info(
        "com.innospots.nexus.console.credential.api",
        "Password decrypt and verification-code ports.",
        main / "credential/api/package-info.java")
    write_package_info(
        "com.innospots.nexus.console.credential.domain.enums",
        "Credential enumerations.",
        main / "credential/domain/enums/package-info.java")


def copy_tree_with_package(src_root: Path, dest_root: Path, old_pkg: str, new_pkg: str,
                           skip_names: set[str] | None = None) -> None:
    skip_names = skip_names or set()
    for src in src_root.rglob("*.java"):
        if src.name in skip_names:
            continue
        rel = src.relative_to(src_root)
        dest = dest_root / rel
        dest.parent.mkdir(parents=True, exist_ok=True)
        text = src.read_text(encoding="utf-8")
        text = text.replace(f"package {old_pkg}", f"package {new_pkg}")
        text = text.replace(old_pkg, new_pkg)
        dest.write_text(text, encoding="utf-8")


def relocate_iam() -> None:
    mapping = [
        ("role", set()),
        ("menu", set()),
        ("permission", {"PermissionResourceSyncService.java", "PermissionResourceSyncServiceTest.java"}),
    ]
    for domain, skip in mapping:
        copy_tree_with_package(
            KERNEL / f"src/main/java/com/innospots/nexus/kernel/{domain}",
            CONSOLE / f"src/main/java/com/innospots/nexus/console/{domain}",
            f"com.innospots.nexus.kernel.{domain}",
            f"com.innospots.nexus.console.{domain}",
            skip,
        )
        copy_tree_with_package(
            KERNEL / f"src/test/java/com/innospots/nexus/kernel/{domain}",
            CONSOLE / f"src/test/java/com/innospots/nexus/console/{domain}",
            f"com.innospots.nexus.kernel.{domain}",
            f"com.innospots.nexus.console.{domain}",
            skip,
        )

    # Remove copied kernel trees except permission sync service.
    for domain in ("role", "menu"):
        shutil.rmtree(KERNEL / f"src/main/java/com/innospots/nexus/kernel/{domain}", ignore_errors=True)
        shutil.rmtree(KERNEL / f"src/test/java/com/innospots/nexus/kernel/{domain}", ignore_errors=True)

    perm_main = KERNEL / "src/main/java/com/innospots/nexus/kernel/permission"
    for path in list(perm_main.rglob("*.java")):
        if path.name != "PermissionResourceSyncService.java":
            path.unlink()
    perm_test = KERNEL / "src/test/java/com/innospots/nexus/kernel/permission"
    for path in list(perm_test.rglob("*.java")):
        if path.name != "PermissionResourceSyncServiceTest.java":
            path.unlink()

    # Drop empty directories left behind.
    for root in (perm_main, perm_test):
        if not root.exists():
            continue
        for directory in sorted(root.rglob("*"), reverse=True):
            if directory.is_dir() and not any(directory.iterdir()):
                directory.rmdir()


GLOBAL_REPLACEMENTS = [
    ("com.innospots.nexus.console.auth.request.",
     "com.innospots.nexus.console.auth.domain.request."),
    ("com.innospots.nexus.console.auth.vo.",
     "com.innospots.nexus.console.auth.domain.vo."),
    ("com.innospots.nexus.console.auth.PlatformAuthEndpoint",
     "com.innospots.nexus.console.auth.endpoint.PlatformAuthEndpoint"),
    ("com.innospots.nexus.console.auth.TenantAuthEndpoint",
     "com.innospots.nexus.console.auth.endpoint.TenantAuthEndpoint"),
    ("com.innospots.nexus.console.auth.UserDirectory",
     "com.innospots.nexus.console.auth.api.UserDirectory"),
    ("com.innospots.nexus.console.auth.CredentialStore",
     "com.innospots.nexus.console.auth.api.CredentialStore"),
    ("com.innospots.nexus.console.auth.MembershipDirectory",
     "com.innospots.nexus.console.auth.api.MembershipDirectory"),
    ("com.innospots.nexus.console.auth.AuthUser",
     "com.innospots.nexus.console.auth.domain.model.AuthUser"),
    ("com.innospots.nexus.console.auth.CredentialRecord",
     "com.innospots.nexus.console.auth.domain.model.CredentialRecord"),
    ("com.innospots.nexus.console.auth.SecurityRealm",
     "com.innospots.nexus.console.auth.domain.enums.SecurityRealm"),
    ("com.innospots.nexus.console.credential.PasswordDecryptor",
     "com.innospots.nexus.console.credential.api.PasswordDecryptor"),
    ("com.innospots.nexus.console.credential.PasswordVerificationOperator",
     "com.innospots.nexus.console.credential.api.PasswordVerificationOperator"),
    ("com.innospots.nexus.console.credential.VerificationType",
     "com.innospots.nexus.console.credential.domain.enums.VerificationType"),
    ("com.innospots.nexus.kernel.role.",
     "com.innospots.nexus.console.role."),
    ("com.innospots.nexus.kernel.menu.",
     "com.innospots.nexus.console.menu."),
    ("com.innospots.nexus.kernel.permission.",
     "com.innospots.nexus.console.permission."),
    ("com.innospots.nexus.kernel.user.domain.vo.UserProfileVO",
     "com.innospots.nexus.kernel.user.domain.vo.UserProfileVo"),
    ("class UserProfileVO", "class UserProfileVo"),
    ("UserProfileVO", "UserProfileVo"),
]


def rewrite_tree(root: Path) -> None:
    if not root.exists():
        return
    for path in root.rglob("*.java"):
        rewrite_text(path, GLOBAL_REPLACEMENTS)


def main() -> None:
    relocate_auth_and_credential()
    relocate_iam()
    for module in (CONSOLE, KERNEL, ROOT / "innospots-nexus-platform"):
        rewrite_tree(module / "src")
    vo = KERNEL / "src/main/java/com/innospots/nexus/kernel/user/domain/vo/UserProfileVO.java"
    renamed = vo.with_name("UserProfileVo.java")
    if vo.exists():
        vo.rename(renamed)
    # Keep kernel permission sync on the kernel package, but import console types.
    sync = KERNEL / "src/main/java/com/innospots/nexus/kernel/permission/service/PermissionResourceSyncService.java"
    if sync.exists():
        text = sync.read_text(encoding="utf-8")
        text = text.replace(
            "package com.innospots.nexus.console.permission.service;",
            "package com.innospots.nexus.kernel.permission.service;",
        )
        sync.write_text(text, encoding="utf-8")
    sync_test = KERNEL / "src/test/java/com/innospots/nexus/kernel/permission/service/PermissionResourceSyncServiceTest.java"
    if sync_test.exists():
        text = sync_test.read_text(encoding="utf-8")
        text = text.replace(
            "package com.innospots.nexus.console.permission.service;",
            "package com.innospots.nexus.kernel.permission.service;",
        )
        sync_test.write_text(text, encoding="utf-8")
    info = KERNEL / "src/main/java/com/innospots/nexus/kernel/permission/package-info.java"
    if info.exists():
        info.write_text(
            "/**\n"
            " * Kernel leftover: permission catalog sync from activated extensions.\n"
            " * Persistence and authorization engine live in console.\n"
            " */\n"
            "package com.innospots.nexus.kernel.permission;\n",
            encoding="utf-8",
        )
    service_info = KERNEL / "src/main/java/com/innospots/nexus/kernel/permission/service/package-info.java"
    if service_info.exists():
        service_info.write_text(
            "/**\n"
            " * Permission catalog sync coordinated with kernel extension runtime.\n"
            " */\n"
            "package com.innospots.nexus.kernel.permission.service;\n",
            encoding="utf-8",
        )


if __name__ == "__main__":
    main()
