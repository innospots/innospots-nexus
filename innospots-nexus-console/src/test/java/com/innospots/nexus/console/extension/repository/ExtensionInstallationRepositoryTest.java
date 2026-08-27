package com.innospots.nexus.console.extension.repository;

import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.core.extension.contract.ConsoleExtensionProvider;
import com.innospots.nexus.core.extension.declaration.ExtensionDescriptor;
import com.innospots.nexus.core.extension.declaration.ExtensionModuleDeclaration;
import com.innospots.nexus.core.extension.declaration.UiSpecPageDeclaration;
import com.innospots.nexus.console.extension.dao.ExtensionInstallationDao;
import com.innospots.nexus.console.extension.domain.entity.ExtensionInstallationEntity;
import com.innospots.nexus.console.extension.domain.enums.ExtensionState;

import static org.assertj.core.api.Assertions.assertThat;

class ExtensionInstallationRepositoryTest {

    @Test
    void registersNewExtensionEnabledByDefaultWithDescriptorSnapshot() {
        DaoFixture fixture = new DaoFixture();
        ExtensionInstallationRepository repository = new ExtensionInstallationRepository(fixture.dao);

        ExtensionInstallationEntity result = repository.register(provider());

        assertThat(result.getExtensionKey()).isEqualTo("com.example.sales");
        assertThat(result.getExtensionVersion()).isEqualTo("1.0.0");
        assertThat(result.isEnabled()).isTrue();
        assertThat(result.getState()).isEqualTo(ExtensionState.REGISTERED.name());
        assertThat(result.getDescriptorSnapshot()).contains("com.example.sales");
        assertThat(fixture.records).containsEntry("com.example.sales", result);
    }

    @Test
    void preservesManagementDisabledChoiceWhenExtensionIsRediscovered() {
        DaoFixture fixture = new DaoFixture();
        ExtensionInstallationEntity existing = new ExtensionInstallationEntity();
        existing.setExtensionKey("com.example.sales");
        existing.setEnabled(false);
        existing.setState(ExtensionState.DISABLED.name());
        fixture.records.put(existing.getExtensionKey(), existing);
        ExtensionInstallationRepository repository = new ExtensionInstallationRepository(fixture.dao);

        ExtensionInstallationEntity result = repository.register(provider());

        assertThat(result.isEnabled()).isFalse();
        assertThat(result.getState()).isEqualTo(ExtensionState.DISABLED.name());
        assertThat(fixture.records.get("com.example.sales")).isSameAs(existing);
    }

    @Test
    void persistsLifecycleStateAndDiagnostics() {
        DaoFixture fixture = new DaoFixture();
        ExtensionInstallationEntity existing = new ExtensionInstallationEntity();
        existing.setExtensionKey("com.example.sales");
        fixture.records.put(existing.getExtensionKey(), existing);
        ExtensionInstallationRepository repository = new ExtensionInstallationRepository(fixture.dao);

        ExtensionInstallationEntity result = repository.updateState(
                "com.example.sales",
                ExtensionState.FAILED,
                true,
                "page path conflict");

        assertThat(result.getState()).isEqualTo(ExtensionState.FAILED.name());
        assertThat(result.isEnabled()).isTrue();
        assertThat(result.getLastError()).isEqualTo("page path conflict");
        assertThat(fixture.records.get("com.example.sales")).isSameAs(existing);
    }

    @Test
    void marksUndiscoveredInstallationAsMissingWithoutChangingDesiredEnablement() {
        DaoFixture fixture = new DaoFixture();
        ExtensionInstallationEntity existing = new ExtensionInstallationEntity();
        existing.setExtensionKey("com.example.sales");
        existing.setEnabled(true);
        existing.setState(ExtensionState.REGISTERED.name());
        fixture.records.put(existing.getExtensionKey(), existing);
        ExtensionInstallationRepository repository = new ExtensionInstallationRepository(fixture.dao);

        assertThat(repository.markMissing(Set.of())).containsExactly(existing);
        assertThat(existing.getState()).isEqualTo(ExtensionState.MISSING.name());
        assertThat(existing.isEnabled()).isTrue();
        assertThat(repository.findAll()).containsExactly(existing);
    }

    private static final class DaoFixture {

        private final Map<String, ExtensionInstallationEntity> records = new HashMap<>();
        private final ExtensionInstallationDao dao = (ExtensionInstallationDao) Proxy.newProxyInstance(
                ExtensionInstallationDao.class.getClassLoader(),
                new Class<?>[]{ExtensionInstallationDao.class},
                (proxy, method, args) -> invoke(method.getName(), args));

        private Object invoke(String methodName, Object[] args) {
            if ("selectByExtensionKey".equals(methodName)) {
                return records.get((String) args[0]);
            }
            if ("selectAll".equals(methodName)) {
                return List.copyOf(records.values());
            }
            if ("insert".equals(methodName)) {
                ExtensionInstallationEntity entity = (ExtensionInstallationEntity) args[0];
                if (entity.getInstallationId() == null) {
                    entity.setInstallationId(UUID.randomUUID().toString().replace("-", ""));
                }
                records.put(entity.getExtensionKey(), entity);
                return 1;
            }
            if ("updateById".equals(methodName)) {
                ExtensionInstallationEntity entity = (ExtensionInstallationEntity) args[0];
                records.put(entity.getExtensionKey(), entity);
                return 1;
            }
            if ("toString".equals(methodName)) {
                return "extension-installation-dao-fixture";
            }
            return defaultValue(methodName);
        }

        private Object defaultValue(String methodName) {
            if (methodName.startsWith("is") || methodName.startsWith("has")) {
                return false;
            }
            return 0;
        }
    }

    private ConsoleExtensionProvider provider() {
        return new ConsoleExtensionProvider() {
            @Override
            public ExtensionDescriptor descriptor() {
                return new ExtensionDescriptor(
                        "com.example.sales",
                        "1.0.0",
                        I18nObject.of("en", "Sales"),
                        I18nObject.of("en", "Sales extension"),
                        List.of(new ExtensionModuleDeclaration(
                                "sales",
                                I18nObject.of("en", "Sales"),
                                I18nObject.of("en", "Sales module"),
                                List.of(new UiSpecPageDeclaration("orders", "/sales/orders", List.of())),
                                List.of())));
            }

            @Override
            public Collection<Class<?>> endpointTypes() {
                return List.of();
            }
        };
    }
}
