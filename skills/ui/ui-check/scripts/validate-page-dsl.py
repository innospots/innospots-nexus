#!/usr/bin/env python3
"""
Validate Pactor Page DSL YAML against JSON Schema and cross-reference rules.

Layers:
  L0  naming (optional, --check-filename)
  L1  JSON Schema shape
  L2  cross-reference closure (dataSource, component, action ids, HTTP methods)

Exit codes:
  0  pass
  1  validation errors
  2  usage / environment errors
"""

from __future__ import annotations

import argparse
import json
import re
import sys
from pathlib import Path
from typing import Any, Iterable

import yaml

try:
    from jsonschema import Draft202012Validator
    from jsonschema.exceptions import SchemaError, ValidationError
except ImportError:
    print(
        "ERROR: jsonschema is required. Run:\n"
        "  pip install -r skills/ui/ui-check/scripts/requirements.txt",
        file=sys.stderr,
    )
    sys.exit(2)


HTTP_METHODS = frozenset(
    {"GET", "POST", "PUT", "PATCH", "DELETE", "HEAD", "OPTIONS"}
)
KEBAB_CASE = re.compile(r"^[a-z][a-z0-9]*(-[a-z0-9]+)*$")

SCRIPT_DIR = Path(__file__).resolve().parent
DEFAULT_SCHEMA = (
    SCRIPT_DIR / "../../ui-reference/references/pactor-page-dsl.schema.yaml"
).resolve()


class Issue:
    def __init__(self, layer: str, path: str, message: str) -> None:
        self.layer = layer
        self.path = path
        self.message = message

    def __str__(self) -> str:
        return f"[{self.layer}] {self.path}: {self.message}"


def load_yaml(path: Path) -> Any:
    with path.open(encoding="utf-8") as handle:
        return yaml.safe_load(handle)


def load_schema(path: Path) -> dict[str, Any]:
    with path.open(encoding="utf-8") as handle:
        schema = yaml.safe_load(handle)
    if not isinstance(schema, dict):
        raise ValueError(f"Schema root must be an object: {path}")
    return schema


def resolve_schema_path(explicit: str | None) -> Path:
    if explicit:
        path = Path(explicit).resolve()
        if not path.is_file():
            raise FileNotFoundError(f"Schema not found: {path}")
        return path
    if DEFAULT_SCHEMA.is_file():
        return DEFAULT_SCHEMA
    raise FileNotFoundError(
        "Schema not found. Pass --schema or ensure "
        "skills/ui/ui-reference/references/pactor-page-dsl.schema.yaml exists."
    )


def format_schema_error(error: ValidationError) -> str:
    location = "/".join(str(part) for part in error.absolute_path)
    if not location:
        location = "(root)"
    return f"{location}: {error.message}"


def validate_schema(document: Any, schema: dict[str, Any]) -> list[Issue]:
    issues: list[Issue] = []
    try:
        validator = Draft202012Validator(schema)
        for error in sorted(validator.iter_errors(document), key=lambda e: list(e.path)):
            issues.append(
                Issue("L1", format_schema_error(error), "schema violation")
            )
    except SchemaError as exc:
        issues.append(Issue("L1", "(schema)", f"invalid schema: {exc}"))
    return issues


def has_text(value: Any) -> bool:
    return isinstance(value, str) and value.strip() != ""


def validate_naming(document: Any, yaml_path: Path | None) -> list[Issue]:
    issues: list[Issue] = []
    if not isinstance(document, dict):
        return issues

    page = document.get("page")
    page_id = page.get("id") if isinstance(page, dict) else None

    if not has_text(page_id):
        issues.append(Issue("L0", "page.id", "required non-empty kebab-case id"))
    elif not KEBAB_CASE.match(page_id):
        issues.append(
            Issue("L0", "page.id", f"'{page_id}' is not kebab-case")
        )

    if yaml_path is not None and has_text(page_id):
        expected = f"{page_id}.yaml"
        actual = yaml_path.name
        if actual not in {expected, f"{page_id}.yml"}:
            issues.append(
                Issue(
                    "L0",
                    "page.id",
                    f"filename '{actual}' does not match page.id '{page_id}'",
                )
            )
    return issues


def iter_actions(action_or_list: Any) -> Iterable[dict[str, Any]]:
    if isinstance(action_or_list, dict):
        yield action_or_list
        return
    if isinstance(action_or_list, list):
        for item in action_or_list:
            if isinstance(item, dict):
                yield item


def map_to_action(value: dict[str, Any]) -> dict[str, Any]:
    action: dict[str, Any] = {}
    if "id" in value and value["id"] is not None:
        action["id"] = str(value["id"])
    if "action" in value and value["action"] is not None:
        action["action"] = str(value["action"])
    params = value.get("params")
    if isinstance(params, dict):
        action["params"] = params
    return action


def validate_branch_actions(
    branch: Any,
    data_sources: dict[str, Any],
    owner: str,
    action_ids: set[str],
    issues: list[Issue],
) -> None:
    if isinstance(branch, list):
        for item in branch:
            if isinstance(item, dict):
                validate_action(map_to_action(item), data_sources, owner, action_ids, issues)
    elif isinstance(branch, dict):
        validate_action(map_to_action(branch), data_sources, owner, action_ids, issues)


def validate_inline_request(params: dict[str, Any], owner: str, issues: list[Issue]) -> None:
    method = params.get("method")
    url = params.get("url")
    if method is not None and str(method).upper() not in HTTP_METHODS:
        issues.append(Issue("L2", owner, f"unsupported HTTP method: {method}"))
    if url is not None and not has_text(str(url)):
        issues.append(Issue("L2", owner, "request url must be non-empty"))


def validate_action_params(
    params: Any,
    data_sources: dict[str, Any],
    owner: str,
    action_ids: set[str],
    issues: list[Issue],
) -> None:
    if not isinstance(params, dict):
        return

    data_source = params.get("dataSource")
    if isinstance(data_source, str) and data_source not in data_sources:
        issues.append(
            Issue("L2", owner, f"unknown dataSource reference: {data_source}")
        )

    if params.get("action") == "request" or "url" in params:
        validate_inline_request(params, owner, issues)

    nested = params.get("actions")
    if isinstance(nested, list):
        for item in nested:
            if isinstance(item, dict):
                validate_action(map_to_action(item), data_sources, owner, action_ids, issues)

    validate_branch_actions(params.get("then"), data_sources, owner, action_ids, issues)
    validate_branch_actions(params.get("else"), data_sources, owner, action_ids, issues)


def validate_action(
    action: dict[str, Any],
    data_sources: dict[str, Any],
    owner: str,
    action_ids: set[str],
    issues: list[Issue],
) -> None:
    if not has_text(action.get("action")):
        issues.append(Issue("L2", owner, "action name is required"))
        return

    action_id = action.get("id")
    if has_text(action_id):
        if action_id in action_ids:
            issues.append(Issue("L2", owner, f"duplicate action id: {action_id}"))
        else:
            action_ids.add(action_id)

    params = action.get("params")
    validate_action_params(params, data_sources, owner, action_ids, issues)


def validate_named_actions(
    actions: Any,
    data_sources: dict[str, Any],
    field: str,
    issues: list[Issue],
) -> None:
    if not isinstance(actions, dict):
        return
    action_ids: set[str] = set()
    for key, value in actions.items():
        if not has_text(key) or value is None:
            issues.append(Issue("L2", field, "action key and definition are required"))
            continue
        for action in iter_actions(value):
            validate_action(action, data_sources, f"{field}.{key}", action_ids, issues)


def validate_http_request(request: Any, owner: str, issues: list[Issue]) -> None:
    if not isinstance(request, dict):
        issues.append(Issue("L2", owner, "http request object is required"))
        return
    if not has_text(request.get("url")):
        issues.append(Issue("L2", owner, "http request url is required"))
    method = request.get("method")
    if method is not None and str(method).upper() not in HTTP_METHODS:
        issues.append(Issue("L2", owner, f"unsupported HTTP method: {method}"))


def validate_data_sources(data_sources: Any, issues: list[Issue]) -> dict[str, Any]:
    if not isinstance(data_sources, dict):
        return {}
    for key, data_source in data_sources.items():
        if not has_text(key) or data_source is None:
            issues.append(Issue("L2", "dataSources", "key and definition are required"))
            continue
        if not isinstance(data_source, dict):
            continue
        ds_type = data_source.get("type")
        if ds_type == "static" and data_source.get("value") is None:
            issues.append(Issue("L2", f"dataSources.{key}", "static.value is required"))
        elif ds_type == "service" and not has_text(data_source.get("service")):
            issues.append(Issue("L2", f"dataSources.{key}", "service.service is required"))
        elif ds_type == "http":
            validate_http_request(data_source.get("request"), f"dataSources.{key}", issues)
    return data_sources


def validate_children(
    children: Any,
    components: dict[str, Any],
    owner: str,
    issues: list[Issue],
) -> None:
    if children is None:
        return
    if isinstance(children, list):
        for index, child in enumerate(children):
            validate_renderable(child, components, f"{owner}[{index}]", issues)
        return
    if isinstance(children, dict):
        if "source" in children:
            validate_renderable(children, components, owner, issues)
            return
        validate_renderable(children, components, owner, issues)


def validate_renderable(
    renderable: Any,
    components: dict[str, Any],
    owner: str,
    issues: list[Issue],
) -> None:
    if renderable is None:
        return
    if not isinstance(renderable, dict):
        return

    if "source" in renderable:
        source = renderable.get("source")
        if source is None:
            issues.append(Issue("L2", owner, "dynamic DSL source is required"))
        placeholder = renderable.get("placeholder")
        if placeholder is not None:
            validate_renderable(placeholder, components, f"{owner}.placeholder", issues)
        return

    if "component" in renderable:
        component_name = renderable.get("component")
        if not has_text(component_name):
            issues.append(Issue("L2", owner, "component reference name is required"))
        elif component_name not in components:
            issues.append(
                Issue("L2", owner, f"unknown component reference: {component_name}")
            )
        children = renderable.get("children")
        if children is not None:
            validate_children(children, components, f"{owner}.children", issues)
        return

    if "type" in renderable:
        if not has_text(renderable.get("type")):
            issues.append(Issue("L2", owner, "component type is required"))
        children = renderable.get("children")
        if children is not None:
            validate_children(children, components, f"{owner}.children", issues)
        return

    issues.append(
        Issue("L2", owner, "renderable must be type, component, or source node")
    )


def validate_components(components: Any, issues: list[Issue]) -> dict[str, Any]:
    if not isinstance(components, dict):
        return {}
    for key, value in components.items():
        if not has_text(key) or value is None:
            issues.append(Issue("L2", "components", "key and definition are required"))
            continue
        validate_renderable(value, components, f"components.{key}", issues)
    return components


def validate_lifecycle(
    lifecycle: Any,
    data_sources: dict[str, Any],
    issues: list[Issue],
) -> None:
    if not isinstance(lifecycle, dict):
        return
    for hook, action_or_list in lifecycle.items():
        for action in iter_actions(action_or_list):
            validate_action(action, data_sources, f"lifecycle.{hook}", set(), issues)


def validate_event_actions(
    events: Any,
    data_sources: dict[str, Any],
    owner: str,
    issues: list[Issue],
) -> None:
    if not isinstance(events, dict):
        return
    for event_name, action_or_list in events.items():
        for action in iter_actions(action_or_list):
            validate_action(action, data_sources, f"{owner}.events.{event_name}", set(), issues)


def walk_event_actions_in_tree(
    node: Any,
    data_sources: dict[str, Any],
    owner: str,
    issues: list[Issue],
) -> None:
    if not isinstance(node, dict):
        return
    events = node.get("events")
    if events is not None:
        validate_event_actions(events, data_sources, owner, issues)
    children = node.get("children")
    if isinstance(children, list):
        for index, child in enumerate(children):
            walk_event_actions_in_tree(
                child, data_sources, f"{owner}.children[{index}]", issues
            )
    elif isinstance(children, dict):
        walk_event_actions_in_tree(children, data_sources, f"{owner}.children", issues)


def validate_cross_references(document: Any) -> list[Issue]:
    issues: list[Issue] = []
    if not isinstance(document, dict):
        return [Issue("L2", "(root)", "document must be a mapping")]

    data_sources = validate_data_sources(document.get("dataSources"), issues)
    components = validate_components(document.get("components"), issues)
    validate_named_actions(document.get("actions"), data_sources, "actions", issues)
    validate_lifecycle(document.get("lifecycle"), data_sources, issues)

    body = document.get("body")
    if body is not None:
        validate_renderable(body, components, "body", issues)
        walk_event_actions_in_tree(body, data_sources, "body", issues)

    children = document.get("children")
    if children is not None:
        validate_children(children, components, "children", issues)
        if isinstance(children, list):
            for index, child in enumerate(children):
                walk_event_actions_in_tree(
                    child, data_sources, f"children[{index}]", issues
                )
        else:
            walk_event_actions_in_tree(children, data_sources, "children", issues)

    for key, value in components.items():
        if isinstance(value, dict):
            walk_event_actions_in_tree(value, data_sources, f"components.{key}", issues)

    return issues


def validate_document(
    document: Any,
    schema: dict[str, Any],
    yaml_path: Path | None,
    check_filename: bool,
) -> list[Issue]:
    issues: list[Issue] = []
    if check_filename:
        issues.extend(validate_naming(document, yaml_path))
    issues.extend(validate_schema(document, schema))
    if not any(issue.layer == "L1" for issue in issues):
        issues.extend(validate_cross_references(document))
    return issues


def print_report(path: Path, issues: list[Issue], json_output: bool) -> int:
    if json_output:
        payload = {
            "file": str(path),
            "passed": len(issues) == 0,
            "issues": [
                {"layer": issue.layer, "path": issue.path, "message": issue.message}
                for issue in issues
            ],
        }
        print(json.dumps(payload, ensure_ascii=False, indent=2))
        return 0 if issues else 1

    if not issues:
        print(f"PASS  {path}")
        return 0

    print(f"FAIL  {path}")
    for issue in issues:
        print(f"  {issue}")
    counts: dict[str, int] = {}
    for issue in issues:
        counts[issue.layer] = counts.get(issue.layer, 0) + 1
    summary = ", ".join(f"{layer}={count}" for layer, count in sorted(counts.items()))
    print(f"  ({len(issues)} issue(s): {summary})")
    return 1


def collect_targets(paths: list[str]) -> list[Path]:
    targets: list[Path] = []
    for raw in paths:
        path = Path(raw)
        if path.is_dir():
            targets.extend(sorted(path.rglob("*.yaml")))
            targets.extend(sorted(path.rglob("*.yml")))
        elif path.is_file():
            targets.append(path)
        else:
            raise FileNotFoundError(f"Path not found: {path}")
    return targets


def main() -> int:
    parser = argparse.ArgumentParser(
        description="Validate Pactor Page DSL YAML against schema and cross-reference rules."
    )
    parser.add_argument(
        "paths",
        nargs="+",
        help="Page YAML file(s) or directory(ies) to validate",
    )
    parser.add_argument(
        "--schema",
        help="Path to pactor-page-dsl.schema.yaml (default: ui-reference/references/)",
    )
    parser.add_argument(
        "--check-filename",
        action="store_true",
        help="L0: require filename to match page.id",
    )
    parser.add_argument(
        "--json",
        action="store_true",
        help="Emit machine-readable JSON report",
    )
    args = parser.parse_args()

    try:
        schema_path = resolve_schema_path(args.schema)
        schema = load_schema(schema_path)
        targets = collect_targets(args.paths)
    except (FileNotFoundError, ValueError) as exc:
        print(f"ERROR: {exc}", file=sys.stderr)
        return 2

    if not targets:
        print("ERROR: no YAML files found", file=sys.stderr)
        return 2

    exit_code = 0
    for target in targets:
        try:
            document = load_yaml(target)
        except yaml.YAMLError as exc:
            print(f"FAIL  {target}")
            print(f"  [L0] (parse): invalid YAML: {exc}")
            exit_code = 1
            continue

        issues = validate_document(
            document,
            schema,
            target,
            args.check_filename,
        )
        result = print_report(target, issues, args.json and len(targets) == 1)
        if result != 0:
            exit_code = 1

    return exit_code


if __name__ == "__main__":
    sys.exit(main())
