package com.innospots.nexus.base.ui.spec;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import lombok.Getter;
import lombok.Setter;

/**
 * Pagination binding for data sources that return paged results.
 *
 * <p>Field values may be literals or expressions bound to page state.</p>
 */
@Getter
@Setter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class PaginationConfig {

    /** Current page index or expression. */
    private Object page;

    /** Page size or expression. */
    private Object pageSize;

    /** Backend field that contains the total count. */
    private String totalField;

    /** Backend field that contains the page records. */
    private String dataField;
}
