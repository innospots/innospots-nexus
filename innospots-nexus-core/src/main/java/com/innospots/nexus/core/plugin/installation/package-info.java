/**
 * 插件安装事实持久化与运行时协调。
 * 安装表记录发现快照和管理员意图，不保存运行时对象；表为全局单表，不含租户字段。
 * 具体 DAO、实体与领域模型分别位于子包 {@code dao}、{@code domain}、{@code repository}、
 * {@code service}。
 */
package com.innospots.nexus.core.plugin.installation;
