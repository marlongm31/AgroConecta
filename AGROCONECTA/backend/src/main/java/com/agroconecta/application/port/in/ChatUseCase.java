package com.agroconecta.application.port.in;

import com.agroconecta.application.command.ChatAttachmentCommand;
import com.agroconecta.application.model.ChatConversation;
import com.agroconecta.application.model.ChatMessage;
import com.agroconecta.application.model.PageQuery;
import com.agroconecta.application.model.PageResult;
import com.agroconecta.application.model.StoredFileContent;

import java.util.List;

public interface ChatUseCase {

    ChatConversation startForProduct(Long userId, Long productId);

    PageResult<ChatConversation> listConversations(Long userId, PageQuery pageQuery);

    ChatConversation getConversation(Long userId, Long conversationId);

    List<ChatMessage> listMessages(Long userId, Long conversationId, Long afterId);

    ChatMessage sendMessage(Long userId, Long conversationId, String content);

    ChatMessage sendMessage(Long userId, Long conversationId, String content, String clientMessageId);

    ChatMessage sendMessage(Long userId, Long conversationId, String content, String clientMessageId,
                            ChatAttachmentCommand attachment);

    ChatMessage sendMessage(Long userId, Long conversationId, String content, String clientMessageId,
                            ChatAttachmentCommand attachment, Long replyToMessageId);

    StoredFileContent downloadAttachment(Long userId, Long conversationId, Long messageId);

    long unreadCount(Long userId);
}
