package com.innospots.nexus.console.logger;

public final class AuditLogAnnotationFixtures {

    private AuditLogAnnotationFixtures() {
    }

    public static AuditLog dictionaryTypeCreate() {
        return new AuditLog() {
            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return AuditLog.class;
            }

            @Override
            public I18nLiteral value() {
                return new I18nLiteral() {
                    @Override
                    public Class<? extends java.lang.annotation.Annotation> annotationType() {
                        return I18nLiteral.class;
                    }

                    @Override
                    public String[] pairs() {
                        return new String[] {"zh-CN", "创建字典类型", "en", "Create dictionary type"};
                    }
                };
            }

            @Override
            public String action() {
                return "dictionary.type.create";
            }

            @Override
            public boolean recordArgs() {
                return true;
            }

            @Override
            public boolean recordResult() {
                return false;
            }

            @Override
            public boolean recordException() {
                return true;
            }
        };
    }
}
