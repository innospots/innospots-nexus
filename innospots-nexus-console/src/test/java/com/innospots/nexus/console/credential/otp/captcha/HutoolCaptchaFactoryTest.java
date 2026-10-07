package com.innospots.nexus.console.credential.otp.captcha;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HutoolCaptchaFactoryTest {

    @Test
    void lineCaptchaProducesCodeAndBase64Image() {
        HutoolCaptchaFactory.GeneratedCaptcha generated = HutoolCaptchaFactory.generate(CaptchaPolicy.DEFAULT);
        assertThat(generated.code()).isNotBlank().hasSize(CaptchaPolicy.DEFAULT.codeCount());
        assertThat(generated.imageBase64()).isNotBlank();
    }

    @Test
    void gifCaptchaUsesGifMimeHintPath() {
        CaptchaPolicy policy = new CaptchaPolicy(120, 50, 4, 0, CaptchaStyle.GIF);
        HutoolCaptchaFactory.GeneratedCaptcha generated = HutoolCaptchaFactory.generate(policy);
        assertThat(generated.code()).hasSize(4);
        assertThat(generated.imageBase64()).isNotBlank();
    }
}
