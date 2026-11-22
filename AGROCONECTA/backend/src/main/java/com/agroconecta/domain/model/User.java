package com.agroconecta.domain.model;

import com.agroconecta.domain.validation.DomainValidator;

import java.time.Instant;
import java.util.Objects;

public record User(
        Long id,
        String fullName,
        String email,
        String passwordHash,
        Role role,
        String phone,
        String location,
        String avatarUrl,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {

    public User {
        fullName = DomainValidator.requiredText(fullName, "nombre completo");
        email = DomainValidator.email(email);
        passwordHash = DomainValidator.requiredText(passwordHash, "contraseña cifrada");
        Objects.requireNonNull(role, "El rol es obligatorio");
        phone = DomainValidator.optionalText(phone);
        location = DomainValidator.optionalText(location);
        avatarUrl = DomainValidator.optionalText(avatarUrl);
    }

    public static User create(String fullName, String email, String passwordHash, Role role,
                              String phone, String location) {
        return new User(null, fullName, email, passwordHash, role, phone, location, null, true, null, null);
    }

    public User updateProfile(String fullName, String phone, String location) {
        return new User(id, fullName, email, passwordHash, role, phone, location, avatarUrl, active, createdAt, updatedAt);
    }

    public User withPasswordHash(String newPasswordHash) {
        return new User(id, fullName, email, newPasswordHash, role, phone, location, avatarUrl, active,
                createdAt, updatedAt);
    }

    public User withStatus(boolean newActive) {
        return new User(id, fullName, email, passwordHash, role, phone, location, avatarUrl, newActive,
                createdAt, updatedAt);
    }

    public User withRole(Role newRole) {
        return new User(id, fullName, email, passwordHash, newRole, phone, location, avatarUrl, active,
                createdAt, updatedAt);
    }

    public User withAvatar(String newAvatarUrl) {
        return new User(id, fullName, email, passwordHash, role, phone, location, newAvatarUrl, active,
                createdAt, updatedAt);
    }

    public boolean isProducer() {
        return role == Role.PRODUCTOR;
    }
}
