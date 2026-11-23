/**
 * 与后端 `CryptoUtils.encryptRsa`（RSA/ECB/OAEPWithSHA-256AndMGF1Padding）
 * 对齐的前端加密。公钥经 `/api/auth/rsa-public-key` 获取（X.509 SPKI Base64）。
 */

const OAEP_SHA256_OVERHEAD_BYTES = 66;
const RSA_2048_MODULUS_BYTES = 256;

type AuthRsaPublicKeyPayload = {
  success?: boolean;
  data?: { publicKeyBase64?: string };
  publicKeyBase64?: string;
};

let cachedPublicKeyBase64: string | null = null;
let inflightPublicKey: Promise<string> | null = null;

function base64ToBytes(base64: string): Uint8Array {
  const binary = atob(base64);
  const bytes = new Uint8Array(binary.length);
  for (let i = 0; i < binary.length; i += 1) {
    bytes[i] = binary.charCodeAt(i);
  }
  return bytes;
}

function bytesToBase64(bytes: Uint8Array): string {
  let binary = "";
  for (let i = 0; i < bytes.length; i += 1) {
    binary += String.fromCharCode(bytes[i]!);
  }
  return btoa(binary);
}

async function importRsaPublicKey(spkiBase64: string): Promise<CryptoKey> {
  const spki = base64ToBytes(spkiBase64);
  const spkiBuffer = spki.buffer.slice(
    spki.byteOffset,
    spki.byteOffset + spki.byteLength,
  ) as ArrayBuffer;
  return crypto.subtle.importKey(
    "spki",
    spkiBuffer,
    { name: "RSA-OAEP", hash: "SHA-256" },
    false,
    ["encrypt"],
  );
}

/** 从 BFF 加载密码加密公钥（带内存缓存）。 */
export async function fetchPasswordEncryptionPublicKey(): Promise<string> {
  if (cachedPublicKeyBase64) {
    return cachedPublicKeyBase64;
  }
  if (!inflightPublicKey) {
    inflightPublicKey = fetch("/api/auth/rsa-public-key", {
      cache: "no-store",
      credentials: "include",
    })
      .then(async (response) => {
        const payload = (await response.json().catch(() => null)) as
          | AuthRsaPublicKeyPayload
          | null;
        const key =
          payload?.data?.publicKeyBase64?.trim() ??
          payload?.publicKeyBase64?.trim();
        if (!response.ok || !key) {
          throw new Error("密码加密公钥不可用");
        }
        cachedPublicKeyBase64 = key;
        return key;
      })
      .finally(() => {
        inflightPublicKey = null;
      });
  }
  return inflightPublicKey;
}

/** 分块 RSA-OAEP 加密，输出 Base64 密文。 */
export async function encryptPasswordForApi(plaintext: string): Promise<string> {
  const keyMaterial = await fetchPasswordEncryptionPublicKey();
  const key = await importRsaPublicKey(keyMaterial);
  const data = new TextEncoder().encode(plaintext);
  const blockSize = RSA_2048_MODULUS_BYTES - OAEP_SHA256_OVERHEAD_BYTES;
  const parts: Uint8Array[] = [];
  for (let offset = 0; offset < data.length; offset += blockSize) {
    const slice = data.subarray(
      offset,
      Math.min(offset + blockSize, data.length),
    );
    const encrypted = await crypto.subtle.encrypt(
      { name: "RSA-OAEP" },
      key,
      slice,
    );
    parts.push(new Uint8Array(encrypted));
  }
  const total = parts.reduce((sum, part) => sum + part.length, 0);
  const merged = new Uint8Array(total);
  let position = 0;
  for (const part of parts) {
    merged.set(part, position);
    position += part.length;
  }
  return bytesToBase64(merged);
}
