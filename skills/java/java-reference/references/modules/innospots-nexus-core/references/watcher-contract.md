# 包 `watcher.contract`

## IWatcher

**类型：** interface

后台 Watcher 接口，继承 Runnable 以便在线程池中执行。 生命周期为循环执行 {@link #check()} → {@link #execute()}，直至被停止。
