package com.emsbarbearia.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.emsbarbearia.dto.PublicLoginRequest;
import com.emsbarbearia.dto.PublicRegisterRequest;
import com.emsbarbearia.entity.Cliente;
import com.emsbarbearia.entity.ClienteCredential;
import com.emsbarbearia.repository.ClienteCredentialRepository;
import com.emsbarbearia.repository.ClienteRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class PublicAuthServiceTest {

    @Mock
    ClienteRepository clienteRepository;

    @Mock
    ClienteCredentialRepository credentialRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    JwtPublicService jwtPublicService;

    @InjectMocks
    PublicAuthService publicAuthService;

    @Test
    void register_shouldCreateClienteAndCredentialAndReturnToken() {
        PublicRegisterRequest request = new PublicRegisterRequest("João", "joao@example.com", "senha123");
        when(credentialRepository.findByProviderAndExternalId("EMAIL", "joao@example.com")).thenReturn(Optional.empty());
        Cliente savedCliente = new Cliente();
        savedCliente.setId(1L);
        savedCliente.setNome("João");
        savedCliente.setEmail("joao@example.com");
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> {
            Cliente c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });
        when(credentialRepository.save(any(ClienteCredential.class))).thenAnswer(inv -> inv.getArgument(0));
        when(passwordEncoder.encode("senha123")).thenReturn("hash");
        when(jwtPublicService.createToken(1L)).thenReturn("jwt-token");

        String token = publicAuthService.register(request);

        assertThat(token).isEqualTo("jwt-token");
        verify(credentialRepository).findByProviderAndExternalId("EMAIL", "joao@example.com");
        verify(clienteRepository).save(any(Cliente.class));
        verify(credentialRepository).save(any(ClienteCredential.class));
        verify(jwtPublicService).createToken(1L);
    }

    @Test
    void register_shouldThrow409WhenEmailAlreadyExists() {
        PublicRegisterRequest request = new PublicRegisterRequest("João", "joao@example.com", "senha123");
        ClienteCredential existing = new ClienteCredential();
        when(credentialRepository.findByProviderAndExternalId("EMAIL", "joao@example.com"))
            .thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> publicAuthService.register(request))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Email já cadastrado");
    }

    @Test
    void login_shouldReturnTokenWhenCredentialsValid() {
        PublicLoginRequest request = new PublicLoginRequest("joao@example.com", "senha123");
        Cliente cliente = new Cliente();
        cliente.setId(1L);
        ClienteCredential cred = new ClienteCredential();
        cred.setCliente(cliente);
        cred.setPasswordHash("hash");
        when(credentialRepository.findByProviderAndExternalId("EMAIL", "joao@example.com"))
            .thenReturn(Optional.of(cred));
        when(passwordEncoder.matches("senha123", "hash")).thenReturn(true);
        when(jwtPublicService.createToken(1L)).thenReturn("jwt-token");

        String token = publicAuthService.login(request);

        assertThat(token).isEqualTo("jwt-token");
        verify(jwtPublicService).createToken(1L);
    }

    @Test
    void login_shouldThrow401WhenEmailNotFound() {
        PublicLoginRequest request = new PublicLoginRequest("joao@example.com", "senha123");
        when(credentialRepository.findByProviderAndExternalId("EMAIL", "joao@example.com"))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> publicAuthService.login(request))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Credenciais inválidas");
    }

    @Test
    void login_shouldThrow401WhenPasswordInvalid() {
        PublicLoginRequest request = new PublicLoginRequest("joao@example.com", "wrong");
        ClienteCredential cred = new ClienteCredential();
        cred.setPasswordHash("hash");
        when(credentialRepository.findByProviderAndExternalId("EMAIL", "joao@example.com"))
            .thenReturn(Optional.of(cred));
        when(passwordEncoder.matches("wrong", "hash")).thenReturn(false);

        assertThatThrownBy(() -> publicAuthService.login(request))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Credenciais inválidas");
    }
}
