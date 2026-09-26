# 包 `contract`

## CapabilityProvider

**类型：** interface

运行时管理的 Capability 实现所遵循的标记和生命周期契约。


## CapabilityProviderContext

**类型：** interface

面向一个已声明 Capability Provider 的专用插件上下文。


## CapabilityProviderFactory

**类型：** interface

无副作用地创建一个全新的、尚未初始化的 Capability Provider。


## Plugin

**类型：** interface

用于声明插件及其插件级生命周期的唯一 classpath SPI。


## PluginContext

**类型：** interface

一次插件启动周期内向插件暴露的只读运行时服务。
