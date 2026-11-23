package com.agroconecta.infrastructure.adapter.out.persistence.mapper;

import com.agroconecta.domain.model.User;
import com.agroconecta.infrastructure.adapter.out.persistence.entity.UserJpaEntity;

public final class UserJpaMapper {

    private UserJpaMapper() {
    }

    public static User toDomain(UserJpaEntity entity) {
        return new User(
                entity.getId(),
                entity.getFullName(),
                entity.getEmail(),
                entity.getPasswordHash(),
                entity.getRole(),
                entity.getPhone(),
                entity.getLocation(),
                entity.getAvatarUrl(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static UserJpaEntity toEntity(User user) {
        UserJpaEntity entity = new UserJpaEntity();
        entity.setId(user.id());
        entity.setFullName(user.fullName());
        entity.setEmail(user.email());
        entity.setPasswordHash(user.passwordHash());
        entity.setRole(user.role());
        entity.setPhone(user.phone());
        entity.setLocation(user.location());
        entity.setAvatarUrl(user.avatarUrl());
        entity.setActive(user.active());
        entity.setCreatedAt(user.createdAt());
        entity.setUpdatedAt(user.updatedAt());
        return entity;
    }
}
