package com.agroconecta.application.command;

import com.agroconecta.domain.model.Role;

public record RegisterCommand(
        String fullName,
        String email,
        String password,
        Role role,
        String phone,
        String location
) {
}
