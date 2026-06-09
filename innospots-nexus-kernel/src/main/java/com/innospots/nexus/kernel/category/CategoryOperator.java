package com.innospots.nexus.kernel.category;

import java.util.List;
import java.util.Optional;

/**
 * Port for category tree operations.
 */
public interface CategoryOperator {

    /**
     * Finds a category by identifier.
     *
     * @param categoryId category identifier
     * @return category when found
     */
    Optional<Category> findCategory(String categoryId);

    /**
     * Lists child categories.
     *
     * @param parentId parent category identifier
     * @return child categories
     */
    List<Category> listChildren(String parentId);

    /**
     * Saves a category.
     *
     * @param category category to save
     * @return saved category
     */
    Category saveCategory(Category category);
}
