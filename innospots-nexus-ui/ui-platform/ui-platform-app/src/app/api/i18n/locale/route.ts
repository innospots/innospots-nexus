import { writeLocaleCookie } from "@/backend/api";

export const dynamic = "force-dynamic";

/**
 * 写入 Console 语言偏好；调用方随后整页刷新以重建 sitemap 与页面。
 * 打包为模块后由 Shell 的 `/api/modules/console/api/i18n/locale` 承接。
 */
export async function POST(request: Request) {
  return writeLocaleCookie(request);
}
