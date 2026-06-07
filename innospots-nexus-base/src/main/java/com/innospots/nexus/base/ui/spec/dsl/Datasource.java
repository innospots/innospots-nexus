package com.innospots.nexus.base.ui.spec.dsl;

import com.innospots.nexus.base.ui.spec.ApiRequest;

public record Datasource(String name, ApiRequest request, String description) {
}
