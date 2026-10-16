package com.innospots.nexus.console.auth.service;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.util.CryptoUtils;
import com.innospots.nexus.console.config.AuthConfig;
import com.innospots.nexus.console.config.AuthConfigRsaMapper;

import static org.assertj.core.api.Assertions.assertThat;

class AuthRsaKeyServiceTest {

    @Test
    void returnsConfiguredPublicKey() {
        CryptoUtils.AsymmetricKeyPair keyPair = CryptoUtils.generateRsaKeyPair();
        AuthConfig authConfig = new AuthConfig();
        authConfig.setRsa(AuthConfigRsaMapper.map(keyPair.publicKey(), keyPair.privateKey()));

        AuthRsaKeyService service = new AuthRsaKeyService(authConfig);

        assertThat(service.getPasswordEncryptionPublicKey().publicKeyBase64())
                .isEqualTo(keyPair.publicKey());
    }

    @Test
    void derivesPublicKeyFromPrivateWhenPublicNotConfigured() {
        CryptoUtils.AsymmetricKeyPair keyPair = CryptoUtils.generateRsaKeyPair();
        AuthConfig authConfig = new AuthConfig();
        authConfig.setRsa(AuthConfigRsaMapper.map(null, keyPair.privateKey()));

        AuthRsaKeyService service = new AuthRsaKeyService(authConfig);

        assertThat(service.getPasswordEncryptionPublicKey().publicKeyBase64())
                .isEqualTo(keyPair.publicKey());
    }
}
