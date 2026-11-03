"use client";

import { useEffect, useMemo, useState } from "react";

import { Button, Input } from "@pactor-app/ui/components";
import { useI18n } from "@pactor-app/runtime";

import { useHostI18n } from "../../i18n/messages";
import {
  formatTimezoneSummary,
  getTimezoneOptions,
} from "../../lib/iana-timezones";
import { saveConsoleTimezone } from "../../lib/platform-console-settings";

export interface ConsoleTimezoneSettingRowProps {
  timezone?: string;
  loading?: boolean | string;
}

function isLoadingFlag(value: boolean | string | undefined): boolean {
  if (value === true) {
    return true;
  }
  if (typeof value === "string") {
    return value === "true" || value === "1";
  }
  return false;
}

/**
 * 系统设置 — 默认时区（全 IANA 列表 + 当前当地时间）。
 */
export function ConsoleTimezoneSettingRow({
  timezone: timezoneProp,
  loading: loadingProp = false,
}: ConsoleTimezoneSettingRowProps) {
  const loading = isLoadingFlag(loadingProp);
  const { t } = useI18n();
  const { locale } = useHostI18n();
  const [timezone, setTimezone] = useState(timezoneProp?.trim() ?? "Asia/Shanghai");
  const [modalOpen, setModalOpen] = useState(false);
  const [query, setQuery] = useState("");
  const [draft, setDraft] = useState(timezone);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [tick, setTick] = useState(0);

  useEffect(() => {
    if (typeof timezoneProp === "string" && timezoneProp.trim().length > 0) {
      setTimezone(timezoneProp.trim());
    }
  }, [timezoneProp]);

  useEffect(() => {
    if (!modalOpen) {
      return;
    }
    setDraft(timezone);
    setQuery("");
    setError(null);
    const timer = window.setInterval(() => {
      setTick((value) => value + 1);
    }, 30_000);
    return () => {
      window.clearInterval(timer);
    };
  }, [modalOpen, timezone]);

  const summary = useMemo(
    () => formatTimezoneSummary(timezone, locale),
    [timezone, locale, tick],
  );

  const options = useMemo(
    () => getTimezoneOptions(locale),
    [locale, tick],
  );

  const filtered = useMemo(() => {
    const needle = query.trim().toLowerCase();
    if (needle.length === 0) {
      return options;
    }
    return options.filter(
      (option) =>
        option.value.toLowerCase().includes(needle) ||
        option.label.toLowerCase().includes(needle),
    );
  }, [options, query]);

  async function onSave() {
    setSaving(true);
    setError(null);
    try {
      await saveConsoleTimezone(draft);
      setTimezone(draft);
      setModalOpen(false);
    } catch (saveError) {
      setError(
        saveError instanceof Error
          ? saveError.message
          : t("common.systemSettings.saveError"),
      );
    } finally {
      setSaving(false);
    }
  }

  return (
    <>
      <div className="system-setting">
        <div>
          <strong>{t("common.systemSettings.timezone.label")}</strong>
          <p>{t("common.systemSettings.timezone.description")}</p>
        </div>
        <div className="system-setting-display">
          <span className="console-timezone-summary">{summary}</span>
          <Button
            variant="link"
            disabled={loading}
            onClick={() => setModalOpen(true)}
          >
            {t("common.settingsPage.edit")}
          </Button>
        </div>
      </div>

      {modalOpen ? (
        <div
          className="console-timezone-modal-backdrop"
          role="presentation"
          onClick={() => {
            if (!saving) {
              setModalOpen(false);
            }
          }}
        >
          <div
            className="console-timezone-modal"
            role="dialog"
            aria-modal="true"
            aria-labelledby="console-timezone-modal-title"
            onClick={(event) => event.stopPropagation()}
          >
            <h3 id="console-timezone-modal-title" className="console-timezone-modal-title">
              {t("common.systemSettings.timezone.label")}
            </h3>
            <Input
              value={query}
              placeholder={t("common.systemSettings.timezone.searchPlaceholder")}
              onChange={(value) => setQuery(String(value))}
            />
            <ul className="console-timezone-list" role="listbox">
              {filtered.map((option) => {
                const selected = option.value === draft;
                return (
                  <li key={option.value}>
                    <button
                      type="button"
                      role="option"
                      aria-selected={selected}
                      className={
                        selected
                          ? "console-timezone-option is-selected"
                          : "console-timezone-option"
                      }
                      onClick={() => setDraft(option.value)}
                    >
                      {option.label}
                    </button>
                  </li>
                );
              })}
            </ul>
            {error ? (
              <p className="console-timezone-error" role="alert">
                {error}
              </p>
            ) : null}
            <div className="console-timezone-modal-actions">
              <Button
                disabled={saving}
                onClick={() => setModalOpen(false)}
              >
                {t("common.settingsPage.cancel")}
              </Button>
              <Button
                variant="primary"
                disabled={saving || draft.length === 0}
                onClick={() => void onSave()}
              >
                {t("common.settingsPage.save")}
              </Button>
            </div>
          </div>
        </div>
      ) : null}
    </>
  );
}
