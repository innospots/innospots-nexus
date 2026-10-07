package com.innospots.nexus.service.websocket.governance;

import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.contract.context.ServiceContext;
import com.innospots.nexus.service.contract.invocation.InvocationContext;
import com.innospots.nexus.service.contract.policy.OperationPolicy;
import com.innospots.nexus.service.contract.security.ResourceRef;
import com.innospots.nexus.service.runtime.invocation.InvocationEngine;
import com.innospots.nexus.service.websocket.message.MessageDescriptor;
import com.innospots.nexus.service.websocket.session.WebSocketSession;

/**
 * 将单条入站 WebSocket 消息经 {@link InvocationEngine#invokeAsync} 派发，使限流、舱壁、熔断、
 * 超时与授权拦截器与 HTTP 路径共享同一套 lease 生命周期。
 *
 * <p><strong>Lease 绑定</strong>：必须在业务 {@link CompletionStage} 完成时释放舱壁许可并记录
 * 熔断结果；因此本类只提供 {@code invokeAsync} 路径，不在同步返回时结束调用。</p>
 *
 * <p><strong>资源引用</strong>：默认 {@code ResourceRef("websocket.message", messageType, scope)}，
 * 供授权拦截器与审计使用；连接级 id 可通过后续 resource resolver 扩展。</p>
 */
public final class WebSocketGovernedMessageDispatcher {

    /** {@link ResourceRef#type()} 的 WebSocket 消息资源类型。 */
    public static final String RESOURCE_TYPE = "websocket.message";

    private final InvocationEngine engine;

    /**
     * 创建派发器。
     *
     * @param engine 已注册治理与安全拦截器的调用引擎
     */
    public WebSocketGovernedMessageDispatcher(InvocationEngine engine) {
        this.engine = Checks.notNull(engine, "engine");
    }

    /**
     * 在引擎保护下执行业务阶段。
     *
     * @param session    当前连接会话（提供 {@link ServiceContext} 与 scope）
     * @param descriptor 已注册消息描述符（含治理键）
     * @param business   通常委托 {@code handler.onMessage(...)}
     * @return 与业务相同的完成阶段；拦截器拒绝时以 failed future 结束
     */
    public CompletionStage<Void> dispatch(
            WebSocketSession<?, ?> session,
            MessageDescriptor descriptor,
            Supplier<CompletionStage<Void>> business) {
        return dispatch(session, descriptor, true, business);
    }

    /**
     * 在引擎保护下执行业务阶段。
     *
     * @param session               当前连接会话
     * @param descriptor            已注册消息描述符
     * @param applyGovernanceKeys   是否应用描述符上的限流/舱壁/熔断/超时键
     * @param business              通常委托 {@code handler.onMessage(...)}
     * @return 与业务相同的完成阶段
     */
    public CompletionStage<Void> dispatch(
            WebSocketSession<?, ?> session,
            MessageDescriptor descriptor,
            boolean applyGovernanceKeys,
            Supplier<CompletionStage<Void>> business) {
        Checks.notNull(session, "session");
        Checks.notNull(descriptor, "descriptor");
        Checks.notNull(business, "business");
        ServiceContext service = session.context().service();
        InvocationContext context = buildContext(service, descriptor, applyGovernanceKeys);
        return engine.invokeAsync(context, business);
    }

    /**
     * 构建单次消息调用的 {@link InvocationContext}。
     *
     * @param service               当前线程已安装的上下文
     * @param descriptor            消息描述符
     * @param applyGovernanceKeys   是否应用治理键
     * @return 调用上下文
     */
    public InvocationContext buildContext(
            ServiceContext service,
            MessageDescriptor descriptor,
            boolean applyGovernanceKeys) {
        Checks.notNull(service, "service");
        Checks.notNull(descriptor, "descriptor");
        OperationPolicy policy = WebSocketMessagePolicies.fromDescriptor(descriptor, applyGovernanceKeys);
        String operationId = WebSocketOperationIds.forMessageType(descriptor.type());
        ResourceRef resource = new ResourceRef(RESOURCE_TYPE, descriptor.type(), service.scope());
        return new InvocationContext(UUID.randomUUID().toString(), operationId, service, policy, resource);
    }
}
