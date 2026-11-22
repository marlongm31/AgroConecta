package com.agroconecta.application.model;

import com.agroconecta.domain.model.User;

public record AuthResult(String token, User user) {
}
