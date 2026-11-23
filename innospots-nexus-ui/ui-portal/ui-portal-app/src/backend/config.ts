/** 服务端转发使用的后端基准地址（spring-portal 示例默认 :8081）。 */
export const serverBaseUrl =
  process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8081";
