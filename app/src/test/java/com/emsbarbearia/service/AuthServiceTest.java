package com.emsbarbearia.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.emsbarbearia.entity.AdminUser;
import com.emsbarbearia.repository.AdminUserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    AdminUserRepository adminUserRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    AuditLogService auditLogService;

    @InjectMocks
    AuthService service;

    @Test
    void login_shouldReturnAuthenticatedWhenCredentialsValid() {
        AdminUser user = new AdminUser();
        user.setId(1L);
        user.setUsername("admin");
        user.setPasswordHash("hash");
        when(adminUserRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "hash")).thenReturn(true);

        String result = service.login("admin", "secret");

        assertThat(result).isEqualTo("authenticated");
        verify(auditLogService).log(eq("POST /auth/login"), eq(null), eq(null));
    }

    @Test
    void login_shouldThrowWhenUserNotFound() {
        when(adminUserRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login("unknown", "pass"))
            .isInstanceOf(AuthService.InvalidCredentialsException.class);
    }

    @Test
    void login_shouldThrowWhenPasswordInvalid() {
        AdminUser user = new AdminUser();
        user.setUsername("admin");
        user.setPasswordHash("hash");
        when(adminUserRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hash")).thenReturn(false);

        assertThatThrownBy(() -> service.login("admin", "wrong"))
            .isInstanceOf(AuthService.InvalidCredentialsException.class);
    }
}
