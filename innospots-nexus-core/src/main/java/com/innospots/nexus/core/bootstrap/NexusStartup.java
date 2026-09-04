package com.innospots.nexus.core.bootstrap;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 启动后初始化编排入口；宿主在配置期组装任务，运行期仅调用 {@link #run()}。
 */
public final class NexusStartup {

    private final List<NexusStartupTask> tasks;

    private NexusStartup(List<NexusStartupTask> tasks) {
        this.tasks = List.copyOf(tasks);
    }

    /** 创建启动编排构建器。 */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 按 {@link NexusStartupTask#order()} 顺序执行全部任务；任一步失败则中止。
     */
    public void run() {
        NexusStartupContext context = new NexusStartupContext();
        List<NexusStartupTask> ordered = tasks.stream()
                .sorted(Comparator.comparingInt(NexusStartupTask::order)
                        .thenComparing(NexusStartupTask::name))
                .toList();
        for (NexusStartupTask task : ordered) {
            task.run(context);
        }
    }

    /** 组装 {@link NexusStartup} 实例。 */
    public static final class Builder {

        private final List<NexusStartupTask> tasks = new ArrayList<>();

        /**
         * 注册一个启动任务。
         *
         * @param task 启动任务
         * @return 当前构建器
         */
        public Builder task(NexusStartupTask task) {
            if (task != null) {
                tasks.add(task);
            }
            return this;
        }

        /**
         * 构建启动编排实例。
         *
         * @return 不可变启动编排
         * @throws IllegalStateException 未注册任何任务时
         */
        public NexusStartup build() {
            if (tasks.isEmpty()) {
                throw new IllegalStateException("at least one startup task is required");
            }
            return new NexusStartup(tasks);
        }
    }
}
