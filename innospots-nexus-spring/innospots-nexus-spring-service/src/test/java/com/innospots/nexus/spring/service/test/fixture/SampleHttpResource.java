package com.innospots.nexus.spring.service.test.fixture;

import java.lang.reflect.Method;
import java.util.Map;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.service.adapter.test.fixture.AdapterDownloadFixtures;
import com.innospots.nexus.service.adapter.test.scenario.AdapterScenarioPaths;
import com.innospots.nexus.service.contract.context.ServiceContextAccessor;
import com.innospots.nexus.service.contract.policy.annotation.RateLimited;
import com.innospots.nexus.service.contract.policy.annotation.ServiceOperation;
import com.innospots.nexus.service.contract.policy.annotation.TimeoutProtected;
import com.innospots.nexus.service.contract.security.annotation.RequiresPermission;
import com.innospots.nexus.service.contract.status.ServiceStatusCode;
import com.innospots.nexus.service.transfer.download.DownloadResource;
import com.innospots.nexus.spring.service.http.invocation.ServiceInvocationBridge;
import com.innospots.nexus.spring.service.http.mvc.ServiceServletFilter;

import jakarta.servlet.http.HttpServletRequest;

/**
 * adapter-test HTTP 夹具端点。
 */
@RestController
@Profile("!webflux")
public class SampleHttpResource {

    private final ServiceInvocationBridge invocationBridge;
    private final ServiceContextAccessor contextAccessor;

    public SampleHttpResource(ServiceInvocationBridge invocationBridge, ServiceContextAccessor contextAccessor) {
        this.invocationBridge = invocationBridge;
        this.contextAccessor = contextAccessor;
    }

    @GetMapping(AdapterScenarioPaths.UNAUGMENTED)
    public Map<String, String> unaugmented() {
        return Map.of("status", "ok");
    }

    @GetMapping(AdapterScenarioPaths.CONTEXT)
    public Map<String, String> context() {
        return Map.of("requestId", contextAccessor.requireCurrent().requestId());
    }

    @GetMapping(AdapterScenarioPaths.ERROR_UNAUTHORIZED)
    public void unauthorized() {
        throw NexusException.build(NexusStatusCode.AUTHENTICATION_FAILED);
    }

    @GetMapping(AdapterScenarioPaths.ERROR_FORBIDDEN)
    public void forbidden() {
        throw NexusException.build(NexusStatusCode.NO_PERMISSION);
    }

    @GetMapping(AdapterScenarioPaths.ERROR_NOT_FOUND)
    public void notFound() {
        throw NexusException.build(NexusStatusCode.RESOURCE_NOT_FOUND);
    }

    @GetMapping(AdapterScenarioPaths.ERROR_RATE_LIMIT)
    public void rateLimitError() {
        throw NexusException.build(NexusStatusCode.LIMIT_EXCEEDED);
    }

    @GetMapping(AdapterScenarioPaths.ERROR_INTERNAL)
    public void internalError() {
        throw NexusException.build(NexusStatusCode.SYSTEM_ERROR);
    }

    @GetMapping(AdapterScenarioPaths.SECURE)
    @ServiceOperation("adapter.secure")
    @RequiresPermission("adapter.secure")
    public Map<String, String> secure() {
        return Map.of("status", "ok");
    }

    @GetMapping(AdapterScenarioPaths.SLOW)
    public Map<String, String> slow(
            @RequestParam(name = "delayMs", defaultValue = "5000") long delayMs,
            HttpServletRequest request) throws NoSuchMethodException {
        Method method = SampleHttpResource.class.getMethod("slow", long.class, HttpServletRequest.class);
        return invocationBridge.invoke(this, method, "adapter.slow", () -> {
            long remaining = delayMs;
            while (remaining > 0) {
                if (contextAccessor.requireCurrent().cancellation().isCancelled()) {
                    ServiceServletFilter.cancelIfClientDisconnected(request);
                    break;
                }
                long step = Math.min(remaining, 50L);
                sleep(step);
                remaining -= step;
            }
            return Map.of("status", "done");
        });
    }

    @GetMapping(AdapterScenarioPaths.GOVERNANCE_RATE_LIMIT)
    @ServiceOperation("adapter.governance.rate-limit")
    @RateLimited("adapter-rate-limit")
    public Map<String, String> governanceRateLimit() {
        return Map.of("status", "ok");
    }

    @GetMapping(AdapterScenarioPaths.GOVERNANCE_TIMEOUT)
    @ServiceOperation("adapter.governance.timeout")
    @TimeoutProtected("adapter.governance.timeout")
    public Map<String, String> governanceTimeout() {
        long remaining = 2_000L;
        while (remaining > 0) {
            if (contextAccessor.requireCurrent().cancellation().isCancelled()) {
                throw NexusException.build(ServiceStatusCode.DEADLINE_EXCEEDED);
            }
            long step = Math.min(remaining, 50L);
            sleep(step);
            remaining -= step;
        }
        return Map.of("status", "ok");
    }

    @GetMapping(AdapterScenarioPaths.OBSERVABILITY_TRACE)
    public Map<String, String> observabilityTrace() {
        return Map.of("status", "ok");
    }

    @GetMapping(AdapterScenarioPaths.DOWNLOAD)
    public DownloadResource download() {
        return AdapterDownloadFixtures.memoryDownload();
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}
