package com.innospots.nexus.base.json;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

class JsonsMaskTest {

    // ---- test records ----

    record Account(
            @MaskValue(MaskStrategy.NAME) String name,
            @MaskValue(MaskStrategy.PHONE) String phone,
            @MaskValue(MaskStrategy.EMAIL) String email,
            @MaskValue(MaskStrategy.ID_CARD) String idCard,
            @MaskValue(MaskStrategy.BANK_CARD) String bankCard,
            @MaskValue(MaskStrategy.PASSWORD) String password,
            @MaskValue String secret,
            @MaskValue(value = MaskStrategy.CUSTOM, keepHead = 2, keepTail = 3) String customField,
            String publicNote
    ) {
    }

    record AccountWithNulls(
            @MaskValue(MaskStrategy.PHONE) String phone,
            @MaskValue(MaskStrategy.EMAIL) String email
    ) {
    }

    record ConvertedValue(
            @ValueConverter(UpperCaseConverter.class) String name,
            @ValueConverter(NumberLabelConverter.class) int count,
            String raw
    ) {
    }

    record ConvertedMaskedValue(
            @ValueConverter(UpperCaseConverter.class)
            @MaskValue(value = MaskStrategy.CUSTOM, keepHead = 1, keepTail = 1)
            String name
    ) {
    }

    static final class UpperCaseConverter implements Function<Object, Object> {

        @Override
        public Object apply(Object value) {
            if (value == null) {
                return null;
            }
            return value.toString().toUpperCase();
        }
    }

    static final class NumberLabelConverter implements Function<Object, Object> {

        @Override
        public Object apply(Object value) {
            return "N-" + value;
        }
    }

    record NestedAccount(
            String name,
            Account account
    ) {
    }

    // ---- unit tests ----

    @Nested
    class StrategyTests {

        @Test
        void phoneStrategy() {
            assertThat(MaskStrategy.PHONE.apply("13812345678"))
                    .isEqualTo("138****5678");
        }

        @Test
        void emailStrategy() {
            assertThat(MaskStrategy.EMAIL.apply("admin@nexus.com"))
                    .isEqualTo("a***@nexus.com");
        }

        @Test
        void emailShortLocal() {
            assertThat(MaskStrategy.EMAIL.apply("a@b.com"))
                    .isEqualTo("a***@b.com");
        }

        @Test
        void emailNoAt() {
            assertThat(MaskStrategy.EMAIL.apply("notanemail"))
                    .isEqualTo("***");
        }

        @Test
        void idCardStrategy() {
            assertThat(MaskStrategy.ID_CARD.apply("320102199001011234"))
                    .isEqualTo("320****1234");
        }

        @Test
        void bankCardStrategy() {
            assertThat(MaskStrategy.BANK_CARD.apply("6222021234567890"))
                    .isEqualTo("6222****7890");
        }

        @Test
        void nameStrategyTwoChars() {
            assertThat(MaskStrategy.NAME.apply("张三"))
                    .isEqualTo("张*");
        }

        @Test
        void nameStrategyThreeChars() {
            assertThat(MaskStrategy.NAME.apply("张三丰"))
                    .isEqualTo("张三*");
        }

        @Test
        void nameStrategyLong() {
            assertThat(MaskStrategy.NAME.apply("欧阳娜娜"))
                    .isEqualTo("欧阳**");
        }

        @Test
        void passwordStrategy() {
            assertThat(MaskStrategy.PASSWORD.apply("s3cret!"))
                    .isEqualTo("******");
        }

        @Test
        void hideStrategy() {
            assertThat(MaskStrategy.HIDE.apply("anything"))
                    .isEqualTo("***");
        }

        @Test
        void nullInput() {
            assertThat(MaskStrategy.PHONE.apply(null)).isNull();
            assertThat(MaskStrategy.EMAIL.apply(null)).isNull();
            assertThat(MaskStrategy.NAME.apply(null)).isNull();
            assertThat(MaskStrategy.PASSWORD.apply(null)).isNull();
        }

        @Test
        void shortInput() {
            assertThat(MaskStrategy.PHONE.apply("12")).isEqualTo("12");
        }
    }

    @Nested
    class AnnotationIntegrationTests {

        @Test
        void masksAllAnnotatedFields() {
            Account account = new Account(
                    "张三",
                    "13812345678",
                    "admin@nexus.com",
                    "320102199001011234",
                    "6222021234567890",
                    "s3cret!",
                    "hidden-value",
                    "CUSTOM-FIELD",
                    "public-note"
            );

            String json = Jsons.toMaskedJson(account);

            assertThat(json).contains("\"name\":\"张*\"");
            assertThat(json).contains("\"phone\":\"138****5678\"");
            assertThat(json).contains("\"email\":\"a***@nexus.com\"");
            assertThat(json).contains("\"idCard\":\"320****1234\"");
            assertThat(json).contains("\"bankCard\":\"6222****7890\"");
            assertThat(json).contains("\"password\":\"******\"");
            assertThat(json).contains("\"secret\":\"***\"");
            assertThat(json).contains("\"customField\":\"CU****ELD\"");
            assertThat(json).contains("\"publicNote\":\"public-note\"");
        }

        @Test
        void nullFieldsAreSerializedAsNull() {
            AccountWithNulls account = new AccountWithNulls(null, null);

            String json = Jsons.toMaskedJson(account);

            assertThat(json).contains("\"phone\":null");
            assertThat(json).contains("\"email\":null");
        }

        @Test
        void regularMapperDoesNotMask() {
            Account account = new Account(
                    "张三",
                    "13812345678",
                    "admin@nexus.com",
                    "320102199001011234",
                    "6222021234567890",
                    "s3cret!",
                    "hidden-value",
                    "CUSTOM-FIELD",
                    "public-note"
            );

            String json = Jsons.toJson(account);

            assertThat(json).contains("\"name\":\"张三\"");
            assertThat(json).contains("\"phone\":\"13812345678\"");
            assertThat(json).contains("\"email\":\"admin@nexus.com\"");
            assertThat(json).doesNotContain("***");
        }

        @Test
        void nestedObjectsAreMasked() {
            Account account = new Account(
                    "李四",
                    "13900001111",
                    "lisi@nexus.com",
                    "110101198001015678",
                    "6217001234567890",
                    "p@ss",
                    "shh",
                    "NES-TEST",
                    "hello"
            );
            NestedAccount nested = new NestedAccount("wrapper", account);

            String json = Jsons.toMaskedJson(nested);

            assertThat(json).contains("\"name\":\"李*\"");
            assertThat(json).contains("\"phone\":\"139****1111\"");
            assertThat(json).contains("\"email\":\"l***@nexus.com\"");
            assertThat(json).contains("\"publicNote\":\"hello\"");
        }

        @Test
        void maskedMapperIsAccessible() {
            assertThat(Jsons.maskedMapper()).isNotNull();
            assertThat(Jsons.maskedMapper()).isSameAs(Jsons.maskedMapper());
        }

        @Test
        void arrayOfMaskedObjects() {
            Account a1 = new Account(
                    "王五", "13700000001", "w1@n.com",
                    "111", "222", "pw", "s1", "CUS1", "note1"
            );
            Account a2 = new Account(
                    "赵六", "13700000002", "w2@n.com",
                    "333", "444", "pw", "s2", "CUS2", "note2"
            );

            String json;
            try {
                json = Jsons.maskedMapper().writeValueAsString(List.of(a1, a2));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            assertThat(json).contains("\"name\":\"王*\"");
            assertThat(json).contains("\"name\":\"赵*\"");
            assertThat(json).contains("\"note1\"");
            assertThat(json).contains("\"note2\"");
        }

        @Test
        void convertsAnnotatedValuesDuringMaskedSerialization() {
            ConvertedValue value = new ConvertedValue("nexus", 7, "origin");

            String json = Jsons.toMaskedJson(value);

            assertThat(json).contains("\"name\":\"NEXUS\"");
            assertThat(json).contains("\"count\":\"N-7\"");
            assertThat(json).contains("\"raw\":\"origin\"");
        }

        @Test
        void regularMapperDoesNotConvertAnnotatedValues() {
            ConvertedValue value = new ConvertedValue("nexus", 7, "origin");

            String json = Jsons.toJson(value);

            assertThat(json).contains("\"name\":\"nexus\"");
            assertThat(json).contains("\"count\":7");
            assertThat(json).contains("\"raw\":\"origin\"");
        }

        @Test
        void convertsBeforeMaskingWhenBothAnnotationsExist() {
            ConvertedMaskedValue value = new ConvertedMaskedValue("nexus");

            String json = Jsons.toMaskedJson(value);

            assertThat(json).contains("\"name\":\"N***S\"");
        }
    }
}
