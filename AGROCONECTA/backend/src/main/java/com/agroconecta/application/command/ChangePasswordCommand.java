package com.agroconecta.application.command;

public record ChangePasswordCommand(String currentPassword, String newPassword) {
}
