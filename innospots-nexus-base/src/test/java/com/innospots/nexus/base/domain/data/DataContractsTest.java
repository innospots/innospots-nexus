package com.innospots.nexus.base.domain.data;

import com.innospots.nexus.base.domain.field.DomainField;
import com.innospots.nexus.base.domain.field.FieldScope;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class DataContractsTest {

    @Test
    void schemaKeepsFieldsAndConfigsTogether() {
        DataSchema schema = DataSchema.named("customer", "Customer")
                .field(DomainField.named("Customer ID", "customer_id", "Long").scope(FieldScope.INPUT))
                .field(DomainField.named("Customer Name", "customer_name", "String"))
                .config("source", "crm");

        assertThat(schema.code()).isEqualTo("customer");
        assertThat(schema.fields()).hasSize(2);
        assertThat(schema.field("customer_id")).isPresent();
        assertThat(schema.config("source")).isEqualTo("crm");
    }

    @Test
    void requestCarriesBodyQueryAndPagingInOneObject() {
        DataRequest<Map<String, Object>> request = DataRequest.<Map<String, Object>>create("customer", DataOperation.QUERY)
                .credentialKey("cred-1")
                .query("country", "CN")
                .body(Map.of("active", true))
                .page(2, 50);

        assertThat(request.target()).isEqualTo("customer");
        assertThat(request.operation()).isEqualTo(DataOperation.QUERY);
        assertThat(request.query("country")).isEqualTo("CN");
        assertThat(request.body()).containsEntry("active", true);
        assertThat(request.pageNo()).isEqualTo(2);
        assertThat(request.pageSize()).isEqualTo(50);
    }

    @Test
    void bodyAndResponseWrapResultMetadataAndPaging() {
        Map<String, Object> row = Map.of("name", "nexus");
        DataBody<List<Map<String, Object>>> body = DataBody.of(List.of(row))
                .schema(DataSchema.named("customer", "Customer"))
                .meta("traceId", "t-1")
                .message("ok")
                .end();

        DataResponse<DataPage<Map<String, Object>>> response = DataResponse.ok(DataPage.of(body.data(), 1, 20, 1))
                .schema(body.schema())
                .meta(body.meta());

        assertThat(body.elapsedMillis()).isGreaterThanOrEqualTo(0);
        assertThat(response.success()).isTrue();
        assertThat(response.data().records()).containsExactly(row);
        assertThat(response.data().pages()).isEqualTo(1);
        assertThat(response.schema().code()).isEqualTo("customer");
        assertThat(response.meta()).containsEntry("traceId", "t-1");
    }

    @Test
    void rejectsInvalidPagingArguments() {
        assertThatIllegalArgumentException().isThrownBy(() -> DataPage.empty(0, 20));
        assertThatIllegalArgumentException().isThrownBy(() -> DataPage.empty(1, 0));
    }
}
