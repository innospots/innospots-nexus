package com.innospots.nexus.platform.dictionary.endpoint;

import java.util.List;

import jakarta.ws.rs.Path;
import lombok.RequiredArgsConstructor;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.domain.response.PageResult;
import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.console.dictionary.domain.request.DictionaryTypeCreateRequest;
import com.innospots.nexus.console.dictionary.domain.request.DictionaryTypePageRequest;
import com.innospots.nexus.console.dictionary.domain.request.DictionaryTypeStatusUpdateRequest;
import com.innospots.nexus.console.dictionary.domain.request.DictionaryTypeUpdateRequest;
import com.innospots.nexus.console.dictionary.domain.vo.DictionaryTypeOptionVo;
import com.innospots.nexus.console.dictionary.domain.vo.DictionaryTypeVo;
import com.innospots.nexus.console.dictionary.endpoint.DictionaryTypeEndpoint;
import com.innospots.nexus.platform.config.PlatformConstant;
import com.innospots.nexus.platform.dictionary.service.PlatformDictionaryService;

/**
 * 运营平台字典类型 REST 实现。
 *
 * <p>HTTP 动词、子路径、OpenAPI 与 grant 元数据均继承 {@link DictionaryTypeEndpoint}；
 * 本类仅绑定 platform 类级 {@link Path} 与文档 {@link Tag}。</p>
 */
@Path(PlatformConstant.DICTIONARY_TYPES_PATH)
@Tag(name = "PlatformDictionary", description = "运营平台字典类型")
@RequiredArgsConstructor
public class PlatformDictionaryTypeEndpoint implements DictionaryTypeEndpoint {

    private final PlatformDictionaryService platformDictionaryService;

    @Override
    public R<PageResult<DictionaryTypeVo>> pageDictionaryTypes(DictionaryTypePageRequest request) {
        return R.ok(platformDictionaryService.pageDictionaryTypes(request));
    }

    @Override
    public R<DictionaryTypeVo> getDictionaryType(String dictionaryTypeId) {
        return R.ok(platformDictionaryService.getDictionaryType(dictionaryTypeId));
    }

    @Override
    public R<DictionaryTypeVo> createDictionaryType(DictionaryTypeCreateRequest request) {
        return R.ok(platformDictionaryService.createDictionaryType(request));
    }

    @Override
    public R<DictionaryTypeVo> updateDictionaryType(
            String dictionaryTypeId,
            DictionaryTypeUpdateRequest request) {
        return R.ok(platformDictionaryService.updateDictionaryType(dictionaryTypeId, request));
    }

    @Override
    public R<Void> updateDictionaryTypeStatus(
            String dictionaryTypeId,
            DictionaryTypeStatusUpdateRequest request) {
        platformDictionaryService.updateDictionaryTypeStatus(dictionaryTypeId, request);
        return R.ok();
    }

    @Override
    public R<Void> deleteDictionaryType(String dictionaryTypeId) {
        platformDictionaryService.deleteDictionaryType(dictionaryTypeId);
        return R.ok();
    }

    @Override
    public R<List<DictionaryTypeOptionVo>> listDictionaryTypeOptions(BasicStatus status) {
        return R.ok(platformDictionaryService.listDictionaryTypeOptions(status));
    }
}
