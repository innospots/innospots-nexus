package com.innospots.nexus.core.plugin.config;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.support.PluginTestLog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SecretValueTest {

    private final PluginTestLog log = new PluginTestLog(SecretValueTest.class, "secret");

    @Test
    void masksSecretInToStringAndSupportsCopyAndUse() {
        SecretValue secret = SecretValue.of("runtime-secret");

        String revealed = secret.<String>use(chars -> new String(chars));
        log.info("secret toString=%s", secret);
        log.info("secret use result=%s", revealed);

        assertThat(secret.toString()).isEqualTo("******");
        assertThat(revealed).isEqualTo("runtime-secret");

        SecretValue copy = secret.copy();
        secret.close();
        assertThat(copy.<String>use(chars -> new String(chars))).isEqualTo("runtime-secret");
        copy.close();
    }

    @Test
    void rejectsBlankSecretsAndNullOperations() {
        assertThatThrownBy(() -> SecretValue.of(null))
                .isInstanceOf(NexusException.class);
        assertThatThrownBy(() -> SecretValue.of("   "))
                .isInstanceOf(NexusException.class);

        SecretValue secret = SecretValue.of("value");
        assertThatThrownBy(() -> secret.use(null))
                .isInstanceOf(NexusException.class);
        secret.close();
    }
}
