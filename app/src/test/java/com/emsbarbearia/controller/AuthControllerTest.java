package com.emsbarbearia.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.emsbarbearia.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    AuthService authService;

    @Test
    void login_shouldReturn200WithTokenWhenCredentialsValid() throws Exception {
        when(authService.login("admin", "password")).thenReturn("authenticated");

        mockMvc.perform(post("/auth/login")
                .contentType(APPLICATION_JSON)
                .content("{\"username\":\"admin\",\"password\":\"password\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("authenticated"));

        verify(authService).login("admin", "password");
    }

    @Test
    void login_shouldReturn401WhenInvalidCredentials() throws Exception {
        when(authService.login(anyString(), anyString()))
            .thenThrow(new AuthService.InvalidCredentialsException());

        mockMvc.perform(post("/auth/login")
                .contentType(APPLICATION_JSON)
                .content("{\"username\":\"wrong\",\"password\":\"wrong\"}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void login_shouldReturn400WhenUsernameBlank() throws Exception {
        mockMvc.perform(post("/auth/login")
                .contentType(APPLICATION_JSON)
                .content("{\"username\":\"\",\"password\":\"pass\"}"))
            .andExpect(status().isBadRequest());
    }
}
