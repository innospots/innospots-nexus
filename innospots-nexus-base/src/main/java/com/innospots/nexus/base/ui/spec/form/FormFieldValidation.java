package com.innospots.nexus.base.ui.spec.form;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.innospots.nexus.base.i18n.I18nObject;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class FormFieldValidation {

    private Boolean required;
    private Integer minLength;
    private Integer maxLength;
    private String pattern;
    private I18nObject message = new I18nObject();

    public FormFieldValidation() {
    }

    public static FormFieldValidation create() {
        return new FormFieldValidation();
    }

    public Boolean required() {
        return required;
    }

    public FormFieldValidation required(Boolean required) {
        this.required = required;
        return this;
    }

    public Integer minLength() {
        return minLength;
    }

    public FormFieldValidation minLength(Integer minLength) {
        this.minLength = minLength;
        return this;
    }

    public Integer maxLength() {
        return maxLength;
    }

    public FormFieldValidation maxLength(Integer maxLength) {
        this.maxLength = maxLength;
        return this;
    }

    public String pattern() {
        return pattern;
    }

    public FormFieldValidation pattern(String pattern) {
        this.pattern = pattern;
        return this;
    }

    public I18nObject message() {
        return message;
    }

    public FormFieldValidation message(I18nObject message) {
        this.message = message == null ? new I18nObject() : message;
        return this;
    }
}
