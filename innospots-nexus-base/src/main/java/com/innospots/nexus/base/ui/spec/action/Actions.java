package com.innospots.nexus.base.ui.spec.action;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Actions {

    private List<UiAction> primary = new ArrayList<>();
    private List<UiAction> secondary = new ArrayList<>();
    private List<UiAction> row = new ArrayList<>();
    private List<UiAction> batch = new ArrayList<>();

    public Actions() {
    }

    public static Actions create() {
        return new Actions();
    }

    public List<UiAction> primary() {
        return List.copyOf(primary);
    }

    public Actions primary(UiAction action) {
        if (action != null) {
            primary.add(action);
        }
        return this;
    }

    public List<UiAction> secondary() {
        return List.copyOf(secondary);
    }

    public Actions secondary(UiAction action) {
        if (action != null) {
            secondary.add(action);
        }
        return this;
    }

    public List<UiAction> row() {
        return List.copyOf(row);
    }

    public Actions row(UiAction action) {
        if (action != null) {
            row.add(action);
        }
        return this;
    }

    public List<UiAction> batch() {
        return List.copyOf(batch);
    }

    public Actions batch(UiAction action) {
        if (action != null) {
            batch.add(action);
        }
        return this;
    }
}
