package com.innospots.nexus.base.domain.field;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FieldContractsTest {

    @Test
    void definesFieldMetadataAndValueTypeConversion() {
        DomainField field = DomainField.named("Customer Name", "customer_name", "String")
                .scope(FieldScope.INPUT)
                .comment("Readable customer name")
                .option(new SelectOption("vip", "VIP"));

        assertThat(field.name()).isEqualTo("Customer Name");
        assertThat(field.code()).isEqualTo("customer_name");
        assertThat(field.valueType()).isEqualTo("String");
        assertThat(field.scope()).isEqualTo(FieldScope.INPUT);
        assertThat(field.options()).containsExactly(new SelectOption("vip", "VIP"));
        assertThat(FieldValueType.INTEGER.convert("12")).isEqualTo(12);
        assertThat(FieldValueType.DATE.convert("2026-06-06")).isEqualTo(LocalDate.of(2026, 6, 6));
    }

    @Test
    void supportsStringFieldIdAndStringValueTypeForNestedSchemas() {
        DomainField field = DomainField.named("Address", "address", "com.innospots.nexus.schema.Address")
                .fieldId("customer.address");

        assertThat(field.fieldId()).isEqualTo("customer.address");
        assertThat(field.valueType()).isEqualTo("com.innospots.nexus.schema.Address");
    }

    @Test
    void supportsSimpleJavaTypeNameAsValueType() {
        DomainField field = DomainField.named("Score", "score", "Double");

        assertThat(field.valueType()).isEqualTo("Double");
    }

    @Test
    void createsParameterFieldWithDefaultValue() {
        ParamField field = ParamField.of("limit", FieldValueType.INTEGER)
                .required(true)
                .defaultValue(20);

        assertThat(field.required()).isTrue();
        assertThat(field.fieldValueType().convert(field.defaultValue())).isEqualTo(20);
        assertThat(field.valueType()).isEqualTo("Integer");
        assertThat(field.options()).isEqualTo(List.of());
    }
}
