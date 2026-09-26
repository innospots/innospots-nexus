# 包 `resource`

## DefaultResourceScope

**类型：** class

线程安全的资源栈，按注册逆序执行每个释放器且每个释放器最多执行一次。

### 方法

#### `manage(T resource) → T`
- **说明：** 注册一个托管资源，作用域关闭时按注册逆序释放。
- **参数：**
  - `resource` — 要托管的自动关闭资源
  - `<T>` — 资源类型
- **返回：** 传入的资源实例

#### `add(Runnable disposer) → synchronized ResourceRegistration`
- **说明：** 注册一个自定义释放器，作用域关闭时按注册逆序执行。
- **参数：**
  - `disposer` — 资源释放逻辑
- **返回：** 可单独关闭的注册句柄

#### `close() → void`
- **说明：** 按注册逆序释放全部资源；重复调用幂等。 / public void close()


## ResourceRegistration

**类型：** interface

一个资源释放器的幂等注册句柄。


## ResourceScope

**类型：** interface

每次启动周期的资源所有权边界，按注册逆序释放资源。
