package com.emsbarbearia.service;

import com.emsbarbearia.entity.AdminUser;
import com.emsbarbearia.repository.AdminUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AdminUserRepository adminUserRepository, PasswordEncoder passwordEncoder) {
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public String login(String username, String password) {
        AdminUser user = adminUserRepository.findByUsername(username)
            .orElseThrow(() -> new InvalidCredentialsException());
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        return "authenticated";
    }

    public static class InvalidCredentialsException extends RuntimeException {
    }
}
