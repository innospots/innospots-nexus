/**
 * 示例运营平台扩展根包。
 * <p>
 * 第一级按<strong>交付面</strong>划分，第二级按<strong>领域</strong>划分，第三级按职责划分：
 * </p>
 * <pre>{@code
 * sample.platform.console.<domain>.endpoint | .service
 * sample.platform.core.<domain>.dao | .domain.* | .operator | .service | .loader
 * sample.platform.inbound.<domain>.endpoint | .service | .<function>
 * }</pre>
 * <p>
 * 参考领域：{@link com.innospots.nexus.sample.platform.core.announcement}、
 * {@link com.innospots.nexus.sample.platform.core.support}。
 * </p>
 */
package com.innospots.nexus.sample.platform;
