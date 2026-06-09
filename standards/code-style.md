# Code Style

## Braces

All `if`, `else`, `for`, `while` blocks must use braces `{}`, even for
single-statement bodies. No bare statements on the same line as the
condition.

```java
// Correct
if (condition) {
    doSomething();
}

// Incorrect
if (condition) doSomething();
if (condition) doSomething();
```

## Indentation

Use 4-space indentation. No tabs.

```java
public class Example {
    public void method() {
        if (condition) {
            for (int i = 0; i < 10; i++) {
                process(i);
            }
        }
    }
}
```

## Line Width

Keep line width reasonable, prefer 120 characters maximum.

```java
// Lines exceeding 120 chars should be broken:
return new NexusException("SOME_LONG_CODE",
        "A descriptive message that would otherwise exceed the line limit");
```

## Import Order

1. `java.*` / `javax.*` — standard library (sorted alphabetically)
2. Third-party imports (sorted alphabetically)
3. `com.innospots.*` — project imports (sorted alphabetically)

Separate groups with a blank line. No wildcard imports.

```java
import java.time.LocalDateTime;
import java.util.List;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonProcessingException;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.json.Jsons;
```

## Lombok

- Implementation classes that use constructor injection should prefer
  `@RequiredArgsConstructor` with `final` dependency fields instead of manually
  writing boilerplate constructors.
- Operator and service implementation classes should include `@Slf4j` so
  logging is available without hand-written logger fields.
- Keep Lombok imports in the third-party import group, before
  `com.innospots.*` project imports.
- Do not use Lombok to hide domain invariants. Records and explicit factories
  remain preferred for immutable request, VO, and model objects.
