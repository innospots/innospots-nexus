package com.innospots.nexus.kernel.user.tools;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.util.CryptoUtils;

import static org.assertj.core.api.Assertions.assertThat;

class UserPasswordDecryptorTest {

    @Test
    void rsaPasswordDecryptorDecryptsFrontendEncryptedPassword() {
        CryptoUtils.AsymmetricKeyPair keyPair = CryptoUtils.generateRsaKeyPair();
        String encryptedPassword = CryptoUtils.encryptRsa("raw-secret", keyPair.publicKey());

        UserPasswordDecryptor decryptor = new RsaUserPasswordDecryptor(keyPair.privateKey());

        assertThat(decryptor.decrypt(encryptedPassword)).isEqualTo("raw-secret");
    }
}
