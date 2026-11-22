/**
 * 将接口返回的验证码图片字段转为可用的 {@code img src}。
 * Hutool 的 {@code getImageBase64Data()} 可能已带 data URI 前缀，需兼容两种形态。
 */
export function resolveCaptchaImageSrc(
    imageBase64?: string | null,
    imageMimeType?: string | null,
): string {
    const raw = imageBase64?.trim() ?? "";
    if (raw.length === 0) {
        return "";
    }
    if (raw.startsWith("data:")) {
        return raw;
    }
    const mime = imageMimeType?.trim() || "image/png";
    return `data:${mime};base64,${raw}`;
}
