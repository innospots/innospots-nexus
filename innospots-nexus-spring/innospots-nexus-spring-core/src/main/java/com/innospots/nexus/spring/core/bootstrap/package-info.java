/**
 * 宿主公共启动引导装配。
 *
 * <p>轻量入口 {@link EnableNexusSimpleBootstrap}（含持久化/事务基础）；完整宿主使用
 * {@link EnableNexusHostBootstrap}（另含系统设置表）。数据源与 DDL 由各 runnable
 * 的 {@code application.yaml} 与本地 {@code resources} 提供，不在此库写死。</p>
 *
 * @author Smars
 * @date 2026/09/13
 */
package com.innospots.nexus.spring.core.bootstrap;

