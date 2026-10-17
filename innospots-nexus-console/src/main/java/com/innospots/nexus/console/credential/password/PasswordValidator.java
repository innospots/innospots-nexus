package com.innospots.nexus.console.credential.password;

import java.util.ArrayList;
import java.util.List;

import com.innospots.nexus.base.i18n.I18nObject;

/**
 * 密码强度校验器。
 * <p>要求：最小长度，且必须包含大写字母、小写字母与数字。</p>
 *
 * @author Smars
 * @date 2026/09/13
 * @see com.innospots.nexus.console.credential.password.service.CredentialService
 */
public class PasswordValidator {

    public static final int MIN_LENGTH = 8;

    /**
     * 当密码满足全部强度要求时返回 true。
     *
     * @param password 待校验密码
     * @return 有效时为 {@code true}，否则为 {@code false}
     */
    public boolean isValid(String password) {
        return policyViolationCodes(password).isEmpty();
    }

    /**
     * 返回未满足的强度规则代码（不含密码内容），供日志与排障使用。
     *
     * @param password 待校验密码
     * @return 违规代码列表；空列表表示通过
     */
    public List<String> policyViolationCodes(String password) {
        List<String> violations = new ArrayList<>();
        if (password == null) {
            violations.add("NULL");
            return violations;
        }
        if (password.length() < MIN_LENGTH) {
            violations.add("TOO_SHORT");
        }
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUpper = true;
            } else if (Character.isLowerCase(c)) {
                hasLower = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            }
        }
        if (!hasUpper) {
            violations.add("MISSING_UPPER");
        }
        if (!hasLower) {
            violations.add("MISSING_LOWER");
        }
        if (!hasDigit) {
            violations.add("MISSING_DIGIT");
        }
        return violations;
    }

    /**
     * 将策略违规代码转为面向用户的说明（不含密码明文）。
     *
     * @param violationCodes {@link #policyViolationCodes} 的结果
     * @return 中英文展示文案
     */
    public I18nObject policyViolationDisplay(List<String> violationCodes) {
        if (violationCodes == null || violationCodes.isEmpty()) {
            return I18nObject.of("en", "Password meets policy", "zh", "密码符合要求");
        }
        List<String> enParts = new ArrayList<>();
        List<String> zhParts = new ArrayList<>();
        for (String code : violationCodes) {
            switch (code) {
                case "NULL" -> {
                    enParts.add("password is required");
                    zhParts.add("密码不能为空");
                }
                case "TOO_SHORT" -> {
                    enParts.add("at least " + MIN_LENGTH + " characters");
                    zhParts.add("长度不少于 " + MIN_LENGTH + " 位");
                }
                case "MISSING_UPPER" -> {
                    enParts.add("at least one uppercase letter");
                    zhParts.add("须包含大写字母");
                }
                case "MISSING_LOWER" -> {
                    enParts.add("at least one lowercase letter");
                    zhParts.add("须包含小写字母");
                }
                case "MISSING_DIGIT" -> {
                    enParts.add("at least one digit");
                    zhParts.add("须包含数字");
                }
                default -> {
                    enParts.add(code);
                    zhParts.add(code);
                }
            }
        }
        String en = "Password does not meet requirements: " + String.join("; ", enParts);
        String zh = "密码不符合要求：" + String.join("；", zhParts);
        return I18nObject.of("en", en, "zh", zh);
    }
}
