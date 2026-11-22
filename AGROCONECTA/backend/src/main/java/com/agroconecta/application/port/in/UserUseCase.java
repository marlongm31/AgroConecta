package com.agroconecta.application.port.in;

import com.agroconecta.application.command.ChangePasswordCommand;
import com.agroconecta.application.command.ImageUploadCommand;
import com.agroconecta.application.command.UpdateProfileCommand;
import com.agroconecta.application.model.PageQuery;
import com.agroconecta.application.model.PageResult;
import com.agroconecta.domain.model.Role;
import com.agroconecta.domain.model.User;

public interface UserUseCase {

    User getProfile(Long userId);

    User updateProfile(Long userId, UpdateProfileCommand command);

    void changePassword(Long userId, ChangePasswordCommand command);

    User updateAvatar(Long userId, ImageUploadCommand command);

    PageResult<User> listUsers(Long requesterId, PageQuery pageQuery);

    User updateStatus(Long requesterId, Long targetUserId, boolean active);

    User updateRole(Long requesterId, Long targetUserId, Role role);
}
