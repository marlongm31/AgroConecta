package com.agroconecta.application.model;

public record StoredFileContent(byte[] content, String contentType, String fileName) {
}
