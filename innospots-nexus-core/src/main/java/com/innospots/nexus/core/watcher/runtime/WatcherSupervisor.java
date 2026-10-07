package com.innospots.nexus.core.watcher.runtime;

import com.innospots.nexus.base.thread.ThreadPoolBuilder;
import com.innospots.nexus.base.thread.NexusThreadPoolExecutor;
import com.innospots.nexus.core.watcher.contract.IWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 管理后台 {@link IWatcher} 线程池的监督器。
 * <p>实现 {@link AutoCloseable}，可通过 try-with-resources 优雅关闭。</p>
 *
 * @author Smars
 * @date 2026/09/13
 * @see IWatcher
 */
public class WatcherSupervisor implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(WatcherSupervisor.class);

    private final int maxSize;
    private final String name;
    private final List<IWatcher> watchers;
    private NexusThreadPoolExecutor executor;

    /**
     * @param maxSize 最大并发 Watcher 数 / 线程池大小
     * @param name    线程池名称，用于线程命名
     */
    public WatcherSupervisor(int maxSize, String name) {
        this.maxSize = maxSize;
        this.name = name;
        this.watchers = new ArrayList<>(maxSize);
        this.executor = ThreadPoolBuilder.build(maxSize, maxSize, 0, name);
    }

    /** 使用默认名称 {@code watcher-supervisor} 创建监督器。 */
    public WatcherSupervisor(int maxSize) {
        this(maxSize, "watcher-supervisor");
    }

    /**
     * 在线程池中注册并启动 Watcher。
     *
     * @param watcher 待注册的 Watcher
     */
    public void register(IWatcher watcher) {
        logger.info("Register watcher: {}", watcher.getClass().getSimpleName());
        if (executor == null) {
            executor = ThreadPoolBuilder.build(maxSize, maxSize, 0, name);
        }
        executor.submit(watcher);
        watchers.add(watcher);
    }

    /** 返回当前已注册的 Watcher 数量。 */
    public int activeCount() {
        return watchers.size();
    }

    /**
     * 优雅关闭：停止全部 Watcher，等待 1 秒后关闭线程池。
     */
    @Override
    public void close() {
        for (IWatcher watcher : watchers) {
            watcher.stop();
        }
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
        watchers.clear();
    }
}
