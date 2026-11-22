package com.agroconecta.application.usecase;

import com.agroconecta.application.command.ChatAttachmentCommand;
import com.agroconecta.application.model.ChatMessage;
import com.agroconecta.application.model.StoredFile;
import com.agroconecta.application.port.out.ConversationRepositoryPort;
import com.agroconecta.application.port.out.FileStoragePort;
import com.agroconecta.application.port.out.MessageRepositoryPort;
import com.agroconecta.application.port.out.ProductRepositoryPort;
import com.agroconecta.application.port.out.UserRepositoryPort;
import com.agroconecta.domain.exception.BusinessRuleException;
import com.agroconecta.domain.model.Conversation;
import com.agroconecta.domain.model.Message;
import com.agroconecta.domain.model.Role;
import com.agroconecta.domain.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatService: adjuntos sin texto y respuestas")
class ChatServiceTest {

    @Mock
    private ConversationRepositoryPort conversationRepository;
    @Mock
    private MessageRepositoryPort messageRepository;
    @Mock
    private ProductRepositoryPort productRepository;
    @Mock
    private UserRepositoryPort userRepository;
    @Mock
    private FileStoragePort fileStorage;
    @Mock
    private IdempotencyService idempotencyService;
    @InjectMocks
    private ChatService chatService;

    @Test
    @DisplayName("Guarda un adjunto sin texto con contenido nulo")
    void attachmentWithoutText() {
        when(conversationRepository.findById(1L)).thenReturn(Optional.of(conversation()));
        when(conversationRepository.save(any(Conversation.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(fileStorage.storeFile("chat", png(), "png", "image/png"))
                .thenReturn(new StoredFile("/uploads/chat/archivo.png", "image/png", 8L));
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> withId(invocation.getArgument(0), 99L));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user()));

        ChatMessage result = chatService.sendMessage(10L, 1L, "  ", null,
                new ChatAttachmentCommand("archivo.png", "image/png", png()));

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(messageRepository).save(captor.capture());
        assertThat(captor.getValue().content()).isNull();
        assertThat(result.content()).isNull();
        assertThat(result.attachment()).isNotNull();
        verify(fileStorage).storeFile("chat", png(), "png", "image/png");
    }

    @Test
    @DisplayName("Asocia la respuesta al mensaje citado de la misma conversación")
    void replyToMessage() {
        when(conversationRepository.findById(1L)).thenReturn(Optional.of(conversation()));
        when(messageRepository.findById(18L)).thenReturn(Optional.of(Message.create(1L, 20L, "Original")));
        when(conversationRepository.save(any(Conversation.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> withId(invocation.getArgument(0), 99L));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user()));

        ChatMessage result = chatService.sendMessage(10L, 1L, "Respuesta", null, null, 18L);

        assertThat(result.replyToMessageId()).isEqualTo(18L);
        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(messageRepository).save(captor.capture());
        assertThat(captor.getValue().replyToMessageId()).isEqualTo(18L);
    }

    @Test
    @DisplayName("Rechaza responder un mensaje de otra conversación")
    void rejectsReplyFromAnotherConversation() {
        when(conversationRepository.findById(1L)).thenReturn(Optional.of(conversation()));
        when(messageRepository.findById(18L)).thenReturn(Optional.of(Message.create(2L, 20L, "Original")));

        assertThatThrownBy(() -> chatService.sendMessage(10L, 1L, "Respuesta", null, null, 18L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("otra conversación");
    }

    private Conversation conversation() {
        Instant now = Instant.now();
        return new Conversation(1L, 5L, 10L, 20L, now, now, now);
    }

    private User user() {
        return User.create("Carlos Pérez", "carlos@agroconecta.co", "hash", Role.COMPRADOR, "300", "Valledupar");
    }

    private byte[] png() {
        return new byte[]{(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A};
    }

    private Message withId(Message message, Long id) {
        return new Message(id, message.conversationId(), message.senderId(), message.content(),
                message.clientMessageId(), message.attachmentUrl(), message.attachmentContentType(),
                message.attachmentSizeBytes(), message.attachmentName(), message.createdAt(),
                message.readAt(), message.replyToMessageId());
    }
}
