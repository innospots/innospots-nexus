"use client";

import { Input } from "@pactor-app/ui/components";

import { saveAiConfig, useAiConfig, type AiProvider } from "../../lib/ai-config";
import { SettingsPage, SettingsSection } from "./settings-layout";

const PROVIDERS: Array<{ value: AiProvider; label: string; hint: string }> = [
  { value: "volcengine", label: "火山方舟", hint: "Ark API，默认豆包系列模型" },
  { value: "openai", label: "OpenAI", hint: "OpenAI 兼容接口" },
  { value: "custom", label: "自定义", hint: "任意 OpenAI 兼容服务" },
];

export function PortalAiSettings() {
  const config = useAiConfig();

  return (
    <SettingsPage
      title="AI 设置"
      description="配置门户使用的模型服务；当前仅保存在浏览器本地"
    >
      <SettingsSection title="模型服务">
        <div className="portal-choice-group" role="radiogroup" aria-label="模型服务">
          {PROVIDERS.map((item) => (
            <button
              key={item.value}
              type="button"
              role="radio"
              aria-checked={config.provider === item.value}
              className={
                config.provider === item.value
                  ? "portal-choice is-active"
                  : "portal-choice"
              }
              onClick={() => saveAiConfig({ ...config, provider: item.value })}
            >
              <span className="portal-choice-label">{item.label}</span>
              <span className="portal-choice-hint">{item.hint}</span>
            </button>
          ))}
        </div>
      </SettingsSection>

      <SettingsSection
        title="连接参数"
        hint="修改后自动保存到当前浏览器；接入服务端配置后会自动同步。"
      >
        <div className="portal-form">
          <label className="portal-field">
            <span className="portal-field-label">服务地址</span>
            <Input
              value={config.baseUrl}
              placeholder="https://ark.cn-beijing.volces.com/api/v3"
              onChange={(value) => saveAiConfig({ ...config, baseUrl: value })}
            />
          </label>
          <label className="portal-field">
            <span className="portal-field-label">模型 / Endpoint ID</span>
            <Input
              value={config.model}
              placeholder="例如 doubao-seed-1-6-250615"
              onChange={(value) => saveAiConfig({ ...config, model: value })}
            />
          </label>
          <label className="portal-field">
            <span className="portal-field-label">API Key</span>
            <Input
              type="password"
              value={config.apiKey}
              placeholder="仅保存在本地浏览器"
              onChange={(value) => saveAiConfig({ ...config, apiKey: value })}
            />
            <span className="portal-field-hint">
              API Key 只保存在 localStorage，不会上传到服务端。
            </span>
          </label>
        </div>
      </SettingsSection>

      <SettingsSection title="状态" hint="后续接入服务端 AI 配置与连通性测试。">
        <dl className="portal-descriptions">
          <div className="portal-descriptions-row">
            <dt>模型服务</dt>
            <dd>
              {PROVIDERS.find((item) => item.value === config.provider)?.label ??
                "—"}
            </dd>
          </div>
          <div className="portal-descriptions-row">
            <dt>模型</dt>
            <dd>{config.model || "未配置"}</dd>
          </div>
          <div className="portal-descriptions-row">
            <dt>API Key</dt>
            <dd>{config.apiKey ? "已保存（本地）" : "未配置"}</dd>
          </div>
        </dl>
      </SettingsSection>
    </SettingsPage>
  );
}
