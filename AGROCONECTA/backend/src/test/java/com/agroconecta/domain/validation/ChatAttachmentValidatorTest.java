package com.agroconecta.domain.validation;

import com.agroconecta.domain.exception.BusinessRuleException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Validador de adjuntos del chat")
class ChatAttachmentValidatorTest {

    private static final byte[] MP4 = new byte[]{0, 0, 0, 0x18, 'f', 't', 'y', 'p', 'i', 's', 'o', 'm'};
    private static final byte[] WEBM = new byte[]{0x1A, 0x45, (byte) 0xDF, (byte) 0xA3,
            'w', 'e', 'b', 'm', 0, 0, 0, 0};

    @Test
    @DisplayName("Acepta video MP4 y WebM por firma y extensión")
    void acceptsVideo() {
        ChatAttachmentValidator.Result mp4 = ChatAttachmentValidator.validate("cosecha.mp4", MP4);
        assertThat(mp4.extension()).isEqualTo("mp4");
        assertThat(mp4.contentType()).isEqualTo("video/mp4");

        ChatAttachmentValidator.Result webm = ChatAttachmentValidator.validate("cosecha.webm", WEBM);
        assertThat(webm.extension()).isEqualTo("webm");
        assertThat(webm.contentType()).isEqualTo("video/webm");
    }

    @Test
    @DisplayName("Distingue notas de voz WebM y M4A según el tipo declarado")
    void acceptsAudio() {
        ChatAttachmentValidator.Result webm = ChatAttachmentValidator.validate("nota.webm", "audio/webm", WEBM);
        assertThat(webm.contentType()).isEqualTo("audio/webm");

        ChatAttachmentValidator.Result m4a = ChatAttachmentValidator.validate("nota.m4a", "audio/mp4", MP4);
        assertThat(m4a.contentType()).isEqualTo("audio/mp4");
        assertThat(m4a.extension()).isEqualTo("m4a");
    }

    @Test
    @DisplayName("Acepta WAV, OGG y MP3 por firma")
    void acceptsCommonAudioFormats() {
        assertThat(ChatAttachmentValidator.validate("nota.wav", wave()).contentType()).isEqualTo("audio/wav");
        assertThat(ChatAttachmentValidator.validate("nota.ogg", ascii("OggS")).contentType()).isEqualTo("audio/ogg");
        assertThat(ChatAttachmentValidator.validate("nota.mp3", ascii("ID3")).contentType()).isEqualTo("audio/mpeg");
    }

    @Test
    @DisplayName("Rechaza extensión que no coincide con el contenido")
    void rejectsExtensionMismatch() {
        assertThatThrownBy(() -> ChatAttachmentValidator.validate("nota.mp4", WEBM))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("extensión del archivo no coincide");
    }

    private static byte[] wave() {
        return new byte[]{'R', 'I', 'F', 'F', 0, 0, 0, 0, 'W', 'A', 'V', 'E'};
    }

    private static byte[] ascii(String value) {
        return value.getBytes(java.nio.charset.StandardCharsets.US_ASCII);
    }
}
