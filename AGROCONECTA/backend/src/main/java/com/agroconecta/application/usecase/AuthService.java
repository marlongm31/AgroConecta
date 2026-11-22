package com.agroconecta.application.usecase;

import com.agroconecta.application.command.LoginCommand;
import com.agroconecta.application.command.RegisterCommand;
import com.agroconecta.application.model.AuthResult;
import com.agroconecta.application.port.in.AuthUseCase;
import com.agroconecta.application.port.out.PasswordHasherPort;
import com.agroconecta.application.port.out.TokenProviderPort;
import com.agroconecta.application.port.out.UserRepositoryPort;
import com.agroconecta.domain.exception.BusinessRuleException;
import com.agroconecta.domain.exception.ConflictException;
import com.agroconecta.domain.exception.ForbiddenException;
import com.agroconecta.domain.exception.InvalidCredentialsException;
import com.agroconecta.domain.model.Role;
import com.agroconecta.domain.model.User;
import com.agroconecta.domain.validation.DomainValidator;

public class AuthService implements AuthUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordHasherPort passwordHasher;
    private final TokenProviderPort tokenProvider;

    public AuthService(UserRepositoryPort userRepository, PasswordHasherPort passwordHasher,
                       TokenProviderPort tokenProvider) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public AuthResult register(RegisterCommand command) {
        Role role = command.role();
        if (role == null || !role.isSelfAssignable()) {
            throw new BusinessRuleException("Solo es posible registrarse como PRODUCTOR o COMPRADOR");
        }
        String email = DomainValidator.email(command.email());
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Ya existe una cuenta registrada con ese correo electrónico");
        }
        String rawPassword = DomainValidator.password(command.password());
        User user = User.create(
                command.fullName(),
                email,
                passwordHasher.hash(rawPassword),
                role,
                command.phone(),
                command.location()
        );
        User saved = userRepository.save(user);
        return new AuthResult(tokenProvider.generateToken(saved), saved);
    }

    @Override
    public AuthResult login(LoginCommand command) {
        String email = DomainValidator.email(command.email());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("Correo o contraseña incorrectos"));
        if (!passwordHasher.matches(command.password(), user.passwordHash())) {
            throw new InvalidCredentialsException("Correo o contraseña incorrectos");
        }
        if (!user.active()) {
            throw new ForbiddenException("La cuenta se encuentra inactiva");
        }
        return new AuthResult(tokenProvider.generateToken(user), user);
    }
}
