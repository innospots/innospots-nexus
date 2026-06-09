package com.innospots.nexus.kernel.category;

/**
 * Hierarchical category entity.
 *
 * @param categoryId category identifier
 * @param parentId   parent category identifier
 * @param code       stable category code
 * @param name       category name
 * @param type       category type
 * @param orderIndex ordering value
 */
public record Category(
        String categoryId,
        String parentId,
        String code,
        String name,
        CategoryType type,
        int orderIndex
) {
}
