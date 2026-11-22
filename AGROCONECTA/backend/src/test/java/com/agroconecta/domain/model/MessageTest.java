package com.agroconecta.domain.model;

import com.agroconecta.domain.exception.BusinessRuleException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Mensajes del chat")
class MessageTest {

    @Test
    @DisplayName("Permite adjuntos sin texto y guarda el contenido como nulo")
    void attachmentWithoutText() {
        Message.Attachment attachment =
                new Message.Attachment("/uploads/chat/archivo.png", "image/png", 24L, "archivo.png");

        Message message = Message.create(1L, 2L, "   ", null, attachment);

        assertThat(message.content()).isNull();
        assertThat(message.attachmentUrl()).isEqualTo("/uploads/chat/archivo.png");
    }

    @Test
    @DisplayName("Exige texto o archivo adjunto")
    void requiresTextOrAttachment() {
        assertThatThrownBy(() -> Message.create(1L, 2L, "   "))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Escribe un mensaje o adjunta un archivo");
    }

    @Test
    @DisplayName("Conserva la referencia al mensaje respondido")
    void keepsReplyReference() {
        Message message = Message.create(1L, 2L, "Respuesta", null, null, 18L);
        assertThat(message.replyToMessageId()).isEqualTo(18L);
    }

    @Test
    @DisplayName("Rechaza una referencia de respuesta inválida")
    void rejectsInvalidReplyReference() {
        assertThatThrownBy(() -> Message.create(1L, 2L, "Respuesta", null, null, 0L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("mensaje respondido");
    }
}
