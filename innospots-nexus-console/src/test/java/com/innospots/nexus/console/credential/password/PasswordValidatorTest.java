package com.innospots.nexus.console.credential.password;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordValidatorTest {

    private final PasswordValidator validator = new PasswordValidator();

    @Test
    void rejectsPasswordShorterThanMinimumLength() {
        assertThat(validator.isValid("Ab1")).isFalse();
        assertThat(PasswordValidator.MIN_LENGTH).isEqualTo(8);
    }

    @Test
    void rejectsPasswordMissingRequiredCharacterClass() {
        assertThat(validator.isValid("abcdefgh")).isFalse();
        assertThat(validator.isValid("ABCDEFGH")).isFalse();
        assertThat(validator.isValid("12345678")).isFalse();
        assertThat(validator.isValid("Abcdefgh")).isFalse();
    }

    @Test
    void acceptsPasswordWithUpperLowerAndDigit() {
        assertThat(validator.isValid("Abcdefg1")).isTrue();
    }

    @Test
    void policyViolationCodesDescribeMissingRules() {
        assertThat(validator.policyViolationCodes("Ab1")).containsExactly("TOO_SHORT");
        assertThat(validator.policyViolationCodes("abcdefgh"))
                .containsExactly("MISSING_UPPER", "MISSING_DIGIT");
        assertThat(validator.policyViolationCodes("Abcdefg1")).isEmpty();
    }

    @Test
    void policyViolationDisplayExplainsRequirementsInChinese() {
        assertThat(validator.policyViolationDisplay(validator.policyViolationCodes("abcdefgh")).cnValue())
                .contains("大写字母")
                .contains("数字");
    }
}
