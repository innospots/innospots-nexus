export type TimezoneOption = {
  value: string;
  label: string;
  offsetMinutes: number;
};

let cachedOptions: TimezoneOption[] | null = null;
let cachedAt = 0;
const CACHE_MS = 60_000;

function listZoneIds(): string[] {
  if (typeof Intl !== "undefined" && "supportedValuesOf" in Intl) {
    return Intl.supportedValuesOf("timeZone");
  }
  return ["UTC", "Asia/Shanghai", "America/New_York", "Europe/London"];
}

/** 当前时刻相对 UTC 的偏移分钟数（含 DST）。 */
export function timezoneOffsetMinutes(zoneId: string, at = new Date()): number {
  try {
    const parts = new Intl.DateTimeFormat("en-US", {
      timeZone: zoneId,
      timeZoneName: "shortOffset",
    }).formatToParts(at);
    const token = parts.find((part) => part.type === "timeZoneName")?.value ?? "GMT";
    const match = token.match(/GMT(?:(\+|-)(\d{1,2})(?::?(\d{2}))?)?/);
    if (!match) {
      return 0;
    }
    if (match[1] === undefined) {
      return 0;
    }
    const sign = match[1] === "-" ? -1 : 1;
    const hours = Number(match[2] ?? 0);
    const minutes = Number(match[3] ?? 0);
    return sign * (hours * 60 + minutes);
  } catch {
    return 0;
  }
}

function formatOffsetLabel(zoneId: string, at = new Date()): string {
  const minutes = timezoneOffsetMinutes(zoneId, at);
  const sign = minutes >= 0 ? "+" : "-";
  const abs = Math.abs(minutes);
  const hours = Math.floor(abs / 60);
  const mins = abs % 60;
  if (mins === 0) {
    return `UTC${sign}${hours}`;
  }
  return `UTC${sign}${hours}:${String(mins).padStart(2, "0")}`;
}

function formatLocalTime(zoneId: string, locale: string, at = new Date()): string {
  try {
    return new Intl.DateTimeFormat(locale, {
      timeZone: zoneId,
      hour: "2-digit",
      minute: "2-digit",
      hour12: false,
    }).format(at);
  } catch {
    return "--:--";
  }
}

/** 列表/下拉选项：偏移 + IANA ID + 当前当地时间。 */
export function formatTimezoneOptionLabel(
  zoneId: string,
  locale: string,
  at = new Date(),
): string {
  const offset = formatOffsetLabel(zoneId, at);
  const time = formatLocalTime(zoneId, locale, at);
  return `${offset} · ${zoneId} · ${time}`;
}

/** 设置页只读展示（单行）。 */
export function formatTimezoneSummary(zoneId: string, locale: string, at = new Date()): string {
  if (!zoneId || zoneId.trim().length === 0) {
    return zoneId;
  }
  const offset = formatOffsetLabel(zoneId.trim(), at);
  const time = formatLocalTime(zoneId.trim(), locale, at);
  return `${zoneId.trim()} (${offset} · ${time})`;
}

export function buildTimezoneOptions(locale: string, at = new Date()): TimezoneOption[] {
  const zones = listZoneIds();
  const options = zones.map((zoneId) => {
    const offsetMinutes = timezoneOffsetMinutes(zoneId, at);
    return {
      value: zoneId,
      offsetMinutes,
      label: formatTimezoneOptionLabel(zoneId, locale, at),
    };
  });
  options.sort((left, right) => {
    if (left.offsetMinutes !== right.offsetMinutes) {
      return left.offsetMinutes - right.offsetMinutes;
    }
    return left.value.localeCompare(right.value);
  });
  return options;
}

export function getTimezoneOptions(locale: string): TimezoneOption[] {
  const now = Date.now();
  if (cachedOptions && now - cachedAt < CACHE_MS) {
    return cachedOptions;
  }
  cachedOptions = buildTimezoneOptions(locale);
  cachedAt = now;
  return cachedOptions;
}
