package com.agroconecta.infrastructure.adapter.in.security;

import com.agroconecta.domain.model.Role;

public record AuthenticatedUser(Long id, String email, Role role) {
}
