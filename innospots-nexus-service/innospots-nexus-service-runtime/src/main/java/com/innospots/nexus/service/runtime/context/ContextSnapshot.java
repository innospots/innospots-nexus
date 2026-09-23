package com.innospots.nexus.service.runtime.context;

import com.innospots.nexus.service.contract.context.ServiceContext;

/**
 * 安装后用于恢复线程的已捕获先前上下文。
 *
 * @param previous 先前上下文，线程无绑定时为 {@code null}
 * @author Smars
 * @date 2026/09/13
 * @see ThreadBoundServiceContext
 */
public record ContextSnapshot(ServiceContext previous) {
}
