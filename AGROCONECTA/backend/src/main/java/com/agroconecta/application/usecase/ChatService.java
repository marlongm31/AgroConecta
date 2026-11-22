package com.agroconecta.application.usecase;

import com.agroconecta.application.command.ChatAttachmentCommand;
import com.agroconecta.application.model.ChatConversation;
import com.agroconecta.application.model.ChatMessage;
import com.agroconecta.application.model.PageQuery;
import com.agroconecta.application.model.PageResult;
import com.agroconecta.application.model.StoredFile;
import com.agroconecta.application.model.StoredFileContent;
import com.agroconecta.application.port.in.ChatUseCase;
import com.agroconecta.application.port.out.ConversationRepositoryPort;
import com.agroconecta.application.port.out.FileStoragePort;
import com.agroconecta.application.port.out.MessageRepositoryPort;
import com.agroconecta.application.port.out.ProductRepositoryPort;
import com.agroconecta.application.port.out.UserRepositoryPort;
import com.agroconecta.domain.exception.BusinessRuleException;
import com.agroconecta.domain.exception.ForbiddenException;
import com.agroconecta.domain.exception.NotFoundException;
import com.agroconecta.domain.model.Conversation;
import com.agroconecta.domain.model.Message;
import com.agroconecta.domain.model.Product;
import com.agroconecta.domain.model.User;
import com.agroconecta.domain.validation.ChatAttachmentValidator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatService implements ChatUseCase {

    private static final int HISTORY_LIMIT = 200;

    private final ConversationRepositoryPort conversationRepository;
    private final MessageRepositoryPort messageRepository;
    private final ProductRepositoryPort productRepository;
    private final UserRepositoryPort userRepository;
    private final FileStoragePort fileStorage;
    private final IdempotencyService idempotencyService;

    public ChatService(ConversationRepositoryPort conversationRepository,
                       MessageRepositoryPort messageRepository,
                       ProductRepositoryPort productRepository,
                       UserRepositoryPort userRepository,
                       FileStoragePort fileStorage,
                       IdempotencyService idempotencyService) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.fileStorage = fileStorage;
        this.idempotencyService = idempotencyService;
    }

    @Override
    public ChatConversation startForProduct(Long userId, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("El producto no existe"));
        if (!product.active()) {
            throw new BusinessRuleException("El producto ya no está disponible para contactar al productor");
        }
        if (product.belongsTo(userId)) {
            throw new BusinessRuleException("No puedes iniciar una conversación sobre tu propio producto");
        }
        Conversation conversation = conversationRepository.findByProductIdAndBuyerId(productId, userId)
                .orElseGet(() -> conversationRepository.save(
                        Conversation.create(productId, userId, product.ownerId())));
        return summary(conversation, userId);
    }

    @Override
    public PageResult<ChatConversation> listConversations(Long userId, PageQuery pageQuery) {
        PageResult<Conversation> page = conversationRepository.findByParticipant(userId, pageQuery);
        return new PageResult<>(page.content().stream().map(conversation -> summary(conversation, userId)).toList(),
                page.page(), page.size(), page.totalElements(), page.totalPages());
    }

    @Override
    public ChatConversation getConversation(Long userId, Long conversationId) {
        return summary(requireConversation(userId, conversationId), userId);
    }

    @Override
    public List<ChatMessage> listMessages(Long userId, Long conversationId, Long afterId) {
        requireConversation(userId, conversationId);
        messageRepository.markConversationRead(conversationId, userId);
        List<Message> messages = afterId == null
                ? messageRepository.findRecent(conversationId, HISTORY_LIMIT)
                : messageRepository.findAfter(conversationId, afterId);
        Map<Long, User> senders = new HashMap<>();
        return messages.stream().map(message -> toChatMessage(message, userId, senders)).toList();
    }

    @Override
    public ChatMessage sendMessage(Long userId, Long conversationId, String content) {
        return sendMessage(userId, conversationId, content, null, null, null);
    }

    @Override
    public ChatMessage sendMessage(Long userId, Long conversationId, String content, String clientMessageId) {
        return sendMessage(userId, conversationId, content, clientMessageId, null, null);
    }

    @Override
    public ChatMessage sendMessage(Long userId, Long conversationId, String content, String clientMessageId,
                                   ChatAttachmentCommand attachment) {
        return sendMessage(userId, conversationId, content, clientMessageId, attachment, null);
    }

    @Override
    public ChatMessage sendMessage(Long userId, Long conversationId, String content, String clientMessageId,
                                   ChatAttachmentCommand attachment, Long replyToMessageId) {
        String key = normalize(clientMessageId);
        Map<Long, User> senders = new HashMap<>();
        if (key == null) {
            return createMessage(userId, conversationId, content, null, attachment, replyToMessageId, senders);
        }
        return idempotencyService.execute(userId, key, "chat_message",
                id -> messageRepository.findById(id).map(message -> toChatMessage(message, userId, senders)),
                () -> createMessage(userId, conversationId, content, key, attachment, replyToMessageId, senders),
                ChatMessage::id);
    }

    @Override
    public StoredFileContent downloadAttachment(Long userId, Long conversationId, Long messageId) {
        requireConversation(userId, conversationId);
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new NotFoundException("Mensaje", messageId));
        if (!message.conversationId().equals(conversationId)) {
            throw new ForbiddenException("El archivo no pertenece a esta conversación");
        }
        if (message.attachmentUrl() == null) {
            throw new BusinessRuleException("El mensaje no tiene archivos adjuntos");
        }
        StoredFileContent file = fileStorage.load(message.attachmentUrl());
        String contentType = message.attachmentContentType() == null ? file.contentType() : message.attachmentContentType();
        String fileName = message.attachmentName() == null ? file.fileName() : message.attachmentName();
        return new StoredFileContent(file.content(), contentType, fileName);
    }

    @Override
    public long unreadCount(Long userId) {
        return messageRepository.countUnreadForParticipant(userId);
    }

    private ChatMessage createMessage(Long userId, Long conversationId, String content, String clientMessageId,
                                      ChatAttachmentCommand attachment, Long replyToMessageId,
                                      Map<Long, User> senders) {
        Conversation conversation = requireConversation(userId, conversationId);
        validateReply(conversationId, replyToMessageId);
        StoredFile stored = null;
        try {
            Message.Attachment storedAttachment = null;
            if (attachment != null) {
                ChatAttachmentValidator.Result validated = ChatAttachmentValidator.validate(
                        attachment.fileName(), attachment.contentType(), attachment.content());
                try {
                    stored = fileStorage.storeFile("chat", attachment.content(),
                            validated.extension(), validated.contentType());
                } catch (RuntimeException exception) {
                    throw new BusinessRuleException("No se pudo guardar el archivo adjunto. Inténtalo de nuevo.");
                }
                storedAttachment = new Message.Attachment(stored.url(), validated.contentType(),
                        (long) attachment.content().length, validated.name());
            }
            Message message = messageRepository.save(
                    Message.create(conversationId, userId, content, clientMessageId, storedAttachment, replyToMessageId));
            conversationRepository.save(conversation.touch(message.createdAt()));
            return toChatMessage(message, userId, senders);
        } catch (RuntimeException exception) {
            if (stored != null) {
                fileStorage.delete(stored.url());
            }
            throw exception;
        }
    }

    private void validateReply(Long conversationId, Long replyToMessageId) {
        if (replyToMessageId == null) {
            return;
        }
        Message original = messageRepository.findById(replyToMessageId)
                .orElseThrow(() -> new NotFoundException("Mensaje", replyToMessageId));
        if (!original.conversationId().equals(conversationId)) {
            throw new BusinessRuleException("El mensaje al que respondes pertenece a otra conversación");
        }
    }

    private Conversation requireConversation(Long userId, Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NotFoundException("La conversación no existe"));
        if (!conversation.includes(userId)) {
            throw new ForbiddenException("No participas en esta conversación");
        }
        return conversation;
    }

    private ChatConversation summary(Conversation conversation, Long userId) {
        Long counterpartId = conversation.counterpartOf(userId);
        User counterpart = userRepository.findById(counterpartId).orElse(null);
        String productName = conversation.productId() == null ? null
                : productRepository.findById(conversation.productId())
                        .map(Product::name)
                        .orElse("Producto no disponible");
        String lastMessage = messageRepository.findLast(conversation.id())
                .map(ChatService::lastMessagePreview)
                .orElse("");
        long unread = messageRepository.countUnreadInConversation(conversation.id(), userId);
        return new ChatConversation(conversation.id(), conversation.productId(), productName, counterpartId,
                counterpart == null ? "Usuario" : counterpart.fullName(),
                counterpart == null ? null : counterpart.avatarUrl(),
                lastMessage, conversation.lastMessageAt(), unread);
    }

    private static String lastMessagePreview(Message message) {
        if (message.content() != null && !message.content().isBlank()) {
            return message.content();
        }
        String contentType = message.attachmentContentType();
        if (contentType != null && contentType.startsWith("audio/")) {
            return "🎤 Audio";
        }
        if (contentType != null && contentType.startsWith("image/")) {
            return "📷 Foto";
        }
        if (contentType != null && contentType.startsWith("video/")) {
            return "🎥 Video";
        }
        return "📄 Documento";
    }

    private ChatMessage toChatMessage(Message message, Long userId, Map<Long, User> senders) {
        User sender = senders.computeIfAbsent(message.senderId(),
                id -> userRepository.findById(id).orElse(null));
        ChatMessage.Attachment attachment = message.attachmentUrl() == null ? null
                : new ChatMessage.Attachment(message.id(), message.attachmentName(),
                message.attachmentContentType(), message.attachmentSizeBytes(),
                "/api/chat/conversations/" + message.conversationId() + "/attachments/" + message.id());
        return new ChatMessage(message.id(), message.conversationId(), message.senderId(),
                sender == null ? "Usuario" : sender.fullName(),
                sender == null ? null : sender.avatarUrl(),
                message.content(), message.clientMessageId(), attachment,
                message.createdAt(), message.readAt(), message.senderId().equals(userId),
                message.replyToMessageId());
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String key = value.trim();
        return key.length() > 120 ? key.substring(0, 120) : key;
    }
}
