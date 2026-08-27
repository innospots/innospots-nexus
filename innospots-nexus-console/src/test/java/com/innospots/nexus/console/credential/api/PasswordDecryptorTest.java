package com.innospots.nexus.console.credential.api;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.util.CryptoUtils;
import com.innospots.nexus.console.credential.RsaPasswordDecryptor;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordDecryptorTest {

    @Test
    void rsaPasswordDecryptorDecryptsFrontendEncryptedPassword() {
        CryptoUtils.AsymmetricKeyPair keyPair = CryptoUtils.generateRsaKeyPair();
        String encryptedPassword = CryptoUtils.encryptRsa("raw-secret", keyPair.publicKey());

        PasswordDecryptor decryptor = new RsaPasswordDecryptor(keyPair.privateKey());

        assertThat(decryptor.decrypt(encryptedPassword)).isEqualTo("raw-secret");
    }
}
