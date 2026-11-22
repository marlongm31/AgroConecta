package com.agroconecta.infrastructure.adapter.in.web;

import com.agroconecta.application.command.ChatAttachmentCommand;
import com.agroconecta.application.model.ChatConversation;
import com.agroconecta.application.model.ChatMessage;
import com.agroconecta.application.model.PageQuery;
import com.agroconecta.application.model.PageResult;
import com.agroconecta.application.model.StoredFileContent;
import com.agroconecta.application.port.in.ChatUseCase;
import com.agroconecta.domain.exception.BusinessRuleException;
import com.agroconecta.infrastructure.adapter.in.security.AuthenticatedUser;
import com.agroconecta.infrastructure.adapter.in.web.dto.ChatDtos.ConversationResponse;
import com.agroconecta.infrastructure.adapter.in.web.dto.ChatDtos.MessageResponse;
import com.agroconecta.infrastructure.adapter.in.web.dto.ChatDtos.SendMessageRequest;
import com.agroconecta.infrastructure.adapter.in.web.dto.ChatDtos.StartConversationRequest;
import com.agroconecta.infrastructure.adapter.in.web.dto.ChatDtos.UnreadCountResponse;
import com.agroconecta.infrastructure.adapter.in.web.dto.CommonDtos.ApiResponse;
import com.agroconecta.infrastructure.adapter.in.web.dto.CommonDtos.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
@Tag(name = "Chat", description = "Conversaciones entre compradores y productores")
public class ChatController {

    private final ChatUseCase chatUseCase;

    public ChatController(ChatUseCase chatUseCase) {
        this.chatUseCase = chatUseCase;
    }

    @PostMapping("/conversations")
    @Operation(summary = "Iniciar o recuperar la conversación de un producto")
    public ResponseEntity<ApiResponse<ConversationResponse>> start(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody StartConversationRequest request) {
        ChatConversation conversation = chatUseCase.startForProduct(user.id(), request.productId());
        return ResponseEntity.ok(ApiResponse.message("Conversación disponible",
                ConversationResponse.from(conversation)));
    }

    @GetMapping("/conversations")
    @Operation(summary = "Listar mis conversaciones")
    public ResponseEntity<ApiResponse<PageResponse<ConversationResponse>>> list(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResult<ChatConversation> result = chatUseCase.listConversations(user.id(), PageQuery.of(page, size));
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(result, ConversationResponse::from)));
    }

    @GetMapping("/conversations/{id}")
    @Operation(summary = "Consultar una conversación propia")
    public ResponseEntity<ApiResponse<ConversationResponse>> get(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(ConversationResponse.from(chatUseCase.getConversation(user.id(), id))));
    }

    @GetMapping("/conversations/{id}/messages")
    @Operation(summary = "Listar mensajes y marcarlos como leídos")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> messages(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long id,
            @RequestParam(required = false) Long after) {
        List<MessageResponse> messages = chatUseCase.listMessages(user.id(), id, after).stream()
                .map(MessageResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(messages));
    }

    @PostMapping("/conversations/{id}/messages")
    @Operation(summary = "Enviar un mensaje")
    public ResponseEntity<ApiResponse<MessageResponse>> send(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long id,
            @Valid @RequestBody SendMessageRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        ChatMessage message = chatUseCase.sendMessage(user.id(), id, request.content(), idempotencyKey,
                null, request.replyToMessageId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(MessageResponse.from(message)));
    }

    @PostMapping(value = "/conversations/{id}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Enviar un mensaje con imagen, vídeo, audio o documento adjunto")
    public ResponseEntity<ApiResponse<MessageResponse>> sendAttachment(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long id,
            @RequestPart(value = "content", required = false) String content,
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "replyToMessageId", required = false) Long replyToMessageId,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new BusinessRuleException("Selecciona un archivo para adjuntar");
        }
        ChatAttachmentCommand attachment = new ChatAttachmentCommand(
                file.getOriginalFilename(), file.getContentType(), file.getBytes());
        ChatMessage message = chatUseCase.sendMessage(user.id(), id, content, idempotencyKey, attachment,
                replyToMessageId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(MessageResponse.from(message)));
    }

    @GetMapping("/conversations/{id}/attachments/{messageId}")
    @Operation(summary = "Descargar un archivo de una conversación propia")
    public ResponseEntity<byte[]> downloadAttachment(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long id,
            @PathVariable Long messageId) {
        StoredFileContent file = chatUseCase.downloadAttachment(user.id(), id, messageId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.contentType()))
                .header(HttpHeaders.CACHE_CONTROL, "private, no-store")
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + file.fileName().replaceAll("[\\r\\n\"]", "_") + "\"")
                .body(file.content());
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Contar los mensajes sin leer del usuario autenticado")
    public ResponseEntity<ApiResponse<UnreadCountResponse>> unread(
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(ApiResponse.ok(new UnreadCountResponse(chatUseCase.unreadCount(user.id()))));
    }
}
