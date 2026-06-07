package com.innospots.nexus.base.ui.spec.table;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.innospots.nexus.base.ui.spec.action.UiAction;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class UiTable {

    private Boolean pagination;
    private Integer pageSize;
    private String displayMode;
    private List<TableColumn> columns = new ArrayList<>();
    private UiAction rowAction;

    public UiTable() {
    }

    public static UiTable create() {
        return new UiTable();
    }

    public Boolean pagination() {
        return pagination;
    }

    public UiTable pagination(Boolean pagination) {
        this.pagination = pagination;
        return this;
    }

    public Integer pageSize() {
        return pageSize;
    }

    public UiTable pageSize(Integer pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public String displayMode() {
        return displayMode;
    }

    public UiTable displayMode(String displayMode) {
        this.displayMode = displayMode;
        return this;
    }

    public List<TableColumn> columns() {
        return List.copyOf(columns);
    }

    public UiTable column(TableColumn column) {
        if (column != null) {
            columns.add(column);
        }
        return this;
    }

    public UiAction rowAction() {
        return rowAction;
    }

    public UiTable rowAction(UiAction rowAction) {
        this.rowAction = rowAction;
        return this;
    }
}
