package com.innospots.nexus.spring.service.http.webflux;

import org.springframework.web.server.ServerWebExchange;

/**
 * 在当前 reactive 请求线程上保存 {@link ServerWebExchange}。
 */
public final class ServiceReactiveExchangeHolder {

    private static final ThreadLocal<ServerWebExchange> CURRENT = new ThreadLocal<>();

    private ServiceReactiveExchangeHolder() {
    }

    /**
     * 绑定当前 exchange。
     *
     * @param exchange WebFlux exchange
     */
    public static void set(ServerWebExchange exchange) {
        CURRENT.set(exchange);
    }

    /**
     * 返回当前 exchange。
     *
     * @return exchange，若不存在则为 {@code null}
     */
    public static ServerWebExchange get() {
        return CURRENT.get();
    }

    /**
     * 清除当前 exchange 绑定。
     */
    public static void clear() {
        CURRENT.remove();
    }
}
