package com.agroconecta.application.command;

public record ChatAttachmentCommand(String fileName, String contentType, byte[] content) {
}
