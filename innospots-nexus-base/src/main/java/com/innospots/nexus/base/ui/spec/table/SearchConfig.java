package com.innospots.nexus.base.ui.spec.table;

import com.innospots.nexus.base.ui.spec.form.FormField;

import java.util.List;

public record SearchConfig(Boolean enabled, String mode, List<FormField> fields) {

    public SearchConfig {
        fields = fields == null ? List.of() : List.copyOf(fields);
    }
}
