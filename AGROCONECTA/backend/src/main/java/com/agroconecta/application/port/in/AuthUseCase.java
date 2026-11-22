package com.agroconecta.application.port.in;

import com.agroconecta.application.command.LoginCommand;
import com.agroconecta.application.command.RegisterCommand;
import com.agroconecta.application.model.AuthResult;

public interface AuthUseCase {

    AuthResult register(RegisterCommand command);

    AuthResult login(LoginCommand command);
}
