package com.emsbarbearia.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.emsbarbearia.config.PublicAuthProperties;
import com.emsbarbearia.config.PublicClienteAuthentication;
import com.emsbarbearia.dto.ClienteResponse;
import com.emsbarbearia.entity.Cliente;
import com.emsbarbearia.repository.ClienteRepository;
import com.emsbarbearia.service.AppleOAuthService;
import com.emsbarbearia.service.GoogleOAuthService;
import com.emsbarbearia.service.OtpService;
import com.emsbarbearia.service.PublicAuthService;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

@WebMvcTest(controllers = PublicAuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class PublicAuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    PublicAuthService publicAuthService;

    @MockBean
    ClienteRepository clienteRepository;

    @MockBean
    GoogleOAuthService googleOAuthService;

    @MockBean
    AppleOAuthService appleOAuthService;

    @MockBean
    OtpService otpService;

    @MockBean
    PublicAuthProperties publicAuthProperties;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void register_shouldReturn200WithTokenWhenValid() throws Exception {
        when(publicAuthService.register(any())).thenReturn("jwt-token-123");

        mockMvc.perform(post("/api/auth/public/register")
                .contextPath("/api")
                .contentType(APPLICATION_JSON)
                .content("{\"nome\":\"João\",\"email\":\"joao@example.com\",\"senha\":\"senha123\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("jwt-token-123"));

        verify(publicAuthService).register(any());
    }

    @Test
    void register_shouldReturn409WhenEmailAlreadyRegistered() throws Exception {
        when(publicAuthService.register(any()))
            .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Email já cadastrado"));

        mockMvc.perform(post("/api/auth/public/register")
                .contextPath("/api")
                .contentType(APPLICATION_JSON)
                .content("{\"nome\":\"João\",\"email\":\"joao@example.com\",\"senha\":\"senha123\"}"))
            .andExpect(status().isConflict());
    }

    @Test
    void login_shouldReturn200WithTokenWhenValid() throws Exception {
        when(publicAuthService.login(any())).thenReturn("jwt-token-456");

        mockMvc.perform(post("/api/auth/public/login")
                .contextPath("/api")
                .contentType(APPLICATION_JSON)
                .content("{\"email\":\"joao@example.com\",\"senha\":\"senha123\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("jwt-token-456"));

        verify(publicAuthService).login(any());
    }

    @Test
    void login_shouldReturn401WhenInvalidCredentials() throws Exception {
        when(publicAuthService.login(any()))
            .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas"));

        mockMvc.perform(post("/api/auth/public/login")
                .contextPath("/api")
                .contentType(APPLICATION_JSON)
                .content("{\"email\":\"joao@example.com\",\"senha\":\"wrong\"}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void me_shouldReturn200AndClienteWhenAuthenticated() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(new PublicClienteAuthentication(1L));
        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNome("João");
        cliente.setEmail("joao@example.com");
        cliente.setTelefone("11999999999");
        cliente.setCreatedAt(Instant.now());
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(publicAuthService.toResponse(any(Cliente.class)))
            .thenReturn(new ClienteResponse(1L, "João", "joao@example.com", "11999999999", Instant.now()));

        mockMvc.perform(get("/api/auth/public/me").contextPath("/api"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.nome").value("João"))
            .andExpect(jsonPath("$.email").value("joao@example.com"));

        verify(clienteRepository).findById(1L);
    }

    @Test
    void me_shouldReturn401WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/auth/public/me").contextPath("/api"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void me_shouldReturn404WhenClienteNotFound() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(new PublicClienteAuthentication(999L));
        when(clienteRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/auth/public/me").contextPath("/api"))
            .andExpect(status().isNotFound());
    }
}
