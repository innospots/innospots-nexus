/**
 * 与后端 {@code CryptoUtils.encryptRsa}（RSA/ECB/OAEPWithSHA-256AndMGF1Padding）对齐的前端加密。
 * 公钥为 X.509 SPKI Base64；默认与 sample {@code console-auth.yaml} 密钥对一致，便于本地联调。
 */
const DEFAULT_SAMPLE_PUBLIC_KEY_BASE64 =
    "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArcIMBPC5VmzPAorOMK/RwOhf7OzmDf5oxiBhrq74i0180BKCcz7t1Phul3jMM1s0QV/NgIs3+XkCoOiNsrT+FbaZDLXKusI87ZLNaagaQQsH3Sq646dPA6boE5VfG0o4DaMiOw87uHJmr0aLWN7usB79q1CppzhKForEOB9suBYyqt2Qhxh1b99UqFEiPNrxZm90/ljlf9Z7Jokv0BQPz21rVyEFMW5pnwaFzq/9NTIQm+UJinEBaG40OPR9mu0tsmsHVCAm9wUbbqt5/oRK7KCnz3jKo5XqQW6K38tHNjGh8NbV68TMNTL7k0vwlsIk3315/myEQPgIUSaZtzuz7wIDAQAB";

const OAEP_SHA256_OVERHEAD_BYTES = 66;
const RSA_2048_MODULUS_BYTES = 256;

function base64ToBytes(base64: string): Uint8Array {
    const binary = atob(base64);
    const bytes = new Uint8Array(binary.length);
    for (let i = 0; i < binary.length; i++) {
        bytes[i] = binary.charCodeAt(i);
    }
    return bytes;
}

function bytesToBase64(bytes: Uint8Array): string {
    let binary = "";
    for (let i = 0; i < bytes.length; i++) {
        binary += String.fromCharCode(bytes[i]);
    }
    return btoa(binary);
}

function resolvePublicKeyBase64(override?: string): string {
    const fromEnv = process.env.NEXT_PUBLIC_NEXUS_RSA_PUBLIC_KEY;
    if (override && override.trim().length > 0) {
        return override.trim();
    }
    if (fromEnv && fromEnv.trim().length > 0) {
        return fromEnv.trim();
    }
    return DEFAULT_SAMPLE_PUBLIC_KEY_BASE64;
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

/**
 * 分块 RSA-OAEP 加密，输出 Base64 密文。
 */
export async function encryptPasswordForApi(
    plaintext: string,
    publicKeyBase64?: string,
): Promise<string> {
    const key = await importRsaPublicKey(resolvePublicKeyBase64(publicKeyBase64));
    const data = new TextEncoder().encode(plaintext);
    const blockSize = RSA_2048_MODULUS_BYTES - OAEP_SHA256_OVERHEAD_BYTES;
    const parts: Uint8Array[] = [];
    for (let offset = 0; offset < data.length; offset += blockSize) {
        const slice = data.subarray(offset, Math.min(offset + blockSize, data.length));
        const encrypted = await crypto.subtle.encrypt({ name: "RSA-OAEP" }, key, slice);
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
