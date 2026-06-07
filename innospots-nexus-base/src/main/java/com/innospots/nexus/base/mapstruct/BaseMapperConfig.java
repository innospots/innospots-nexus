package com.innospots.nexus.base.mapstruct;

import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.MapperConfig;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;

/**
 * Shared MapStruct mapper configuration used by all domain mappers.
 * Configures accessor-only collection mapping, always-on null checks,
 * and silent ignoring of unmapped target fields.
 */
@MapperConfig(
        collectionMappingStrategy = CollectionMappingStrategy.ACCESSOR_ONLY,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface BaseMapperConfig {
}
