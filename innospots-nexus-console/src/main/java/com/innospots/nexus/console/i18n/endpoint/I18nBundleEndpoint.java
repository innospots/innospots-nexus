package com.innospots.nexus.console.i18n.endpoint;

import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.console.config.ConsoleConstant;
import com.innospots.nexus.console.entry.ConsolePageRoutes;
import com.innospots.nexus.console.i18n.domain.vo.MessageBundleSetVo;
import com.innospots.nexus.console.i18n.loader.MessageBundleLoader;
import com.innospots.nexus.console.i18n.service.MessageBundleService;
import com.innospots.nexus.core.endpoint.grant.EndpointGrant;
import com.innospots.nexus.core.endpoint.grant.EndpointGrantAction;
import com.innospots.nexus.core.endpoint.grant.EndpointModule;
import com.innospots.nexus.core.endpoint.grant.I18nLiteral;

/**
 * Console 公共语言包加载接口，不要求登录或功能授权。
 *
 * <p>接口仅返回 UI 文案与内容版本，不返回用户、权限或运行时敏感数据。</p>
 *
 * @author Smars
 * @date 2026/10/26
 * @see MessageBundleService
 */
@EndpointModule(
        value = "i18n",
        domain = ConsolePageRoutes.BUILTIN_DOMAIN_KEY,
        ignoreAuth = true,
        ignorePermission = true)
@Path(ConsoleConstant.PUBLIC_API_PREFIX + "/i18n/bundles")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "I18n", description = "语言包加载")
public final class I18nBundleEndpoint {

    private final MessageBundleService service;

    /**
     * 为 JAX-RS 扫描创建使用默认 classpath 加载器的端点。
     */
    public I18nBundleEndpoint() {
        this(new MessageBundleService(new MessageBundleLoader()));
    }

    /**
     * 使用显式语言包服务创建端点。
     *
     * @param service 语言包服务
     */
    public I18nBundleEndpoint(MessageBundleService service) {
        this.service = service;
    }

    /**
     * 加载指定 locale 的 Console 语言包集合。
     *
     * @param appId 应用标识，缺省 console
     * @param locale 显式语言，支持 zh、zh_CN、en、en_US 等别名
     * @param namespaces 逗号分隔 namespace；缺省加载全部
     * @param release 发布标识
     * @param cookieLocale 语言偏好 Cookie
     * @param acceptLanguage Accept-Language 回退
     * @return 语言包集合
     */
    @GET
    @EndpointGrant(
            action = EndpointGrantAction.READ,
            name = "i18n.bundles",
            label = @I18nLiteral(pairs = {"zh-CN", "加载控制台语言包", "en", "Load console message bundles"}),
            recordArgs = false,
            auditLog = false,
            ignoreAuth = true,
            ignorePermission = true)
    @Operation(operationId = "i18nBundles", summary = "加载控制台语言包")
    public R<MessageBundleSetVo> bundles(
            @Parameter(description = "应用标识，当前仅支持 console")
            @QueryParam("appId") String appId,
            @Parameter(description = "语言代码；支持 zh/zh_CN 与 en/en_US 别名")
            @QueryParam("locale") String locale,
            @Parameter(description = "逗号分隔的命名空间；缺省返回全部")
            @QueryParam("namespaces") String namespaces,
            @Parameter(description = "应用发布标识，回显到协议 envelope")
            @QueryParam("release") String release,
            @Parameter(description = "浏览器语言偏好 Cookie")
            @CookieParam("locale") String cookieLocale,
            @Parameter(description = "标准 Accept-Language 回退链")
            @HeaderParam("Accept-Language") String acceptLanguage
    ) {
        return R.ok(service.load(
                appId,
                locale,
                cookieLocale,
                acceptLanguage,
                namespaces,
                release));
    }
}
