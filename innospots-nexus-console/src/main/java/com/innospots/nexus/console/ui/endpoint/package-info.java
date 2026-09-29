/**
 * Pactor Page DSL 渲染端口与 Jakarta REST 暴露。
 *
 * <ul>
 *   <li>{@link PageDslEndpoint} — 程序化渲染端口</li>
 *   <li>{@link DefaultPageDslEndpoint} — 默认实现与
 *       {@code GET /api/public/pages/{pageKey}}?…}（{@code pageKey}={domain}-{module}-{xxx}）</li>
 * </ul>
 *
 * <p>HTTP 请求体见 {@code com.innospots.nexus.console.ui.domain.request.PageDslRenderRequest}；
 * 响应为 {@code R<PageDsl>}，构建期 OpenAPI 扫描本包与 {@code ui.spec}。</p>
 */
package com.innospots.nexus.console.ui.endpoint;
