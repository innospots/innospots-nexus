package com.innospots.nexus.core.watcher.runtime;

import com.innospots.nexus.base.util.DateTimeUtils;
import com.innospots.nexus.core.watcher.contract.IWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 后台 Watcher 的模板方法基类。
 * <p>循环执行 {@link #check()} → {@link #execute()} → 休眠，直至调用 {@link #stop()}
 * 或 {@code runningCondition} 返回 {@code false}。具体工作由子类通过 {@link #execute()} 定义。</p>
 *
 * @author Smars
 * @date 2026/09/13
 * @see IWatcher
 */
public abstract class AbstractWatcher implements IWatcher {

    private static final Logger logger = LoggerFactory.getLogger(AbstractWatcher.class);

    private final String name;
    private final int checkIntervalMillis;
    private final Supplier<Boolean> runningCondition;
    private volatile boolean running;
    private long startTimeMillis;

    /**
     * @param name                Watcher 名称，用于日志
     * @param checkIntervalMillis 周期间隔休眠时间（毫秒）
     * @param runningCondition    每轮检查的运行条件；为 {@code false} 时退出循环
     */
    protected AbstractWatcher(String name, int checkIntervalMillis, Supplier<Boolean> runningCondition) {
        this.name = name;
        this.checkIntervalMillis = checkIntervalMillis;
        this.runningCondition = runningCondition;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    /**
     * 主执行循环：检查 → 执行 → 休眠，重复直至停止或运行条件为 {@code false}。
     */
    @Override
    public void run() {
        running = true;
        startTimeMillis = System.currentTimeMillis();
        logger.info("Watcher started: {}", name);

        int interval = checkIntervalMillis;
        while (running && runningCondition.get()) {
            try {
                if (check()) {
                    interval = execute();
                }
                // execute() 未返回有效间隔时回退到默认值
                if (interval <= 0) {
                    interval = checkIntervalMillis;
                }
            } catch (Exception e) {
                logger.error("Watcher {} execution error: {}", name, e.getMessage(), e);
            } finally {
                try {
                    TimeUnit.MILLISECONDS.sleep(interval);
                } catch (InterruptedException e) {
                    logger.error("Watcher {} interrupted", name, e);
                    Thread.currentThread().interrupt();
                    running = false;
                }
            }
            // 休眠后重新检查条件，防止状态已变更
            running = runningCondition.get();
        }

        running = false;
        logger.info("Watcher {} stopped, uptime: {}", name, DateTimeUtils.consume(startTimeMillis));
    }

    /** 将运行标志设为 {@code false}；循环将在当前轮结束后退出。 */
    @Override
    public void stop() {
        running = false;
    }
}
