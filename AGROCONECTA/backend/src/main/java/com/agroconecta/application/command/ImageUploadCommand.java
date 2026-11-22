package com.agroconecta.application.command;

public record ImageUploadCommand(String fileName, String contentType, byte[] content) {
}
