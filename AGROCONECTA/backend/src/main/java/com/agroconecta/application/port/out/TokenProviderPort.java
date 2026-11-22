package com.agroconecta.application.port.out;

import com.agroconecta.domain.model.User;

public interface TokenProviderPort {

    String generateToken(User user);
}
