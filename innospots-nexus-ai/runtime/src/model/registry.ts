import { createModels, type Api, type Model, type MutableModels } from "@earendil-works/pi-ai";
import { getBuiltinModel } from "@earendil-works/pi-ai/providers/all";
import { openaiProvider } from "@earendil-works/pi-ai/providers/openai";
import { anthropicProvider } from "@earendil-works/pi-ai/providers/anthropic";
import { googleProvider } from "@earendil-works/pi-ai/providers/google";
import { deepseekProvider } from "@earendil-works/pi-ai/providers/deepseek";
import { openrouterProvider } from "@earendil-works/pi-ai/providers/openrouter";
import type { ConfigSnapshot, ModelDef, ProviderDef } from "../config/loader.js";
import type { Secrets } from "../security/secrets.js";

export interface ResolvedModel {
  model: Model<Api>;
  apiKey?: string;
  provider: ProviderDef;
  modelDef: ModelDef;
}

export class ModelRegistry {
  private readonly snapshot: ConfigSnapshot;
  private readonly models: MutableModels;
  private readonly secrets: Secrets;
  private readonly overrideModel?: Model<Api>;

  constructor(snapshot: ConfigSnapshot, secrets: Secrets, models?: MutableModels, overrideModel?: Model<Api>) {
    this.snapshot = snapshot;
    this.secrets = secrets;
    this.models = models ?? createModels();
    this.overrideModel = overrideModel;
    if (!models) this.registerProviders();
  }

  getModels(): MutableModels {
    return this.models;
  }

  resolve(agentModel: string): ResolvedModel {
    const modelDef = this.snapshot.models.get(agentModel);
    if (!modelDef || !modelDef.enabled) throw new Error(`model_not_configured: ${agentModel}`);
    const provider = this.snapshot.providers.get(modelDef.provider);
    if (!provider || !provider.enabled) throw new Error(`provider_not_configured: ${modelDef.provider}`);
    const apiKey = this.resolveApiKey(provider);
    if (this.overrideModel) {
      return { model: this.overrideModel, apiKey, provider, modelDef };
    }
    const model = this.toPiModel(modelDef, provider);
    return { model, apiKey, provider, modelDef };
  }

  private registerProviders(): void {
    this.models.setProvider(openaiProvider());
    this.models.setProvider(anthropicProvider());
    this.models.setProvider(googleProvider());
    this.models.setProvider(deepseekProvider());
    this.models.setProvider(openrouterProvider());
  }

  private toPiModel(def: ModelDef, provider: ProviderDef): Model<Api> {
    let base: Model<Api> | undefined;
    try {
      base = getBuiltinModel(provider.type === "openai-compatible" ? "openai" : provider.type as any, def.modelId as any) as Model<Api>;
    } catch {
      base = undefined;
    }
    if (base) {
      return {
        ...base,
        id: def.modelId,
        name: def.displayName ?? def.modelId,
        baseUrl: provider.baseUrl ?? base.baseUrl,
        headers: { ...base.headers, ...provider.headers },
        contextWindow: def.contextWindow ?? base.contextWindow,
        maxTokens: def.maxOutputTokens ?? base.maxTokens,
      };
    }
    return {
      id: def.modelId,
      name: def.displayName ?? def.modelId,
      api: "openai-completions",
      provider: "openai",
      baseUrl: provider.baseUrl ?? "https://api.openai.com/v1",
      input: ["text"],
      cost: { input: 0, output: 0, cacheRead: 0, cacheWrite: 0 },
      reasoning: false,
      contextWindow: def.contextWindow ?? 128000,
      maxTokens: def.maxOutputTokens ?? 8192,
      headers: provider.headers,
    } as Model<Api>;
  }

  private resolveApiKey(provider: ProviderDef): string | undefined {
    const fromEnv = provider.apiKey?.fromEnv;
    if (fromEnv) {
      const secret = this.secrets.get(fromEnv);
      if (secret == null) throw new Error(`missing secret ${fromEnv}`);
      return secret;
    }
    const secretRef = provider.apiKey?.secretRef;
    if (secretRef) {
      const secret = this.secrets.get(secretRef);
      if (secret == null) throw new Error(`missing secret ${secretRef}`);
      return secret;
    }
    return undefined;
  }
}
