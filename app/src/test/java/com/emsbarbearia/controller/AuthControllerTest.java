package com.emsbarbearia.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void login_shouldReturn200WithToken() throws Exception {
        mockMvc.perform(post("/auth/login")
                .contentType(APPLICATION_JSON)
                .content("{\"username\":\"admin\",\"password\":\"secret\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value(startsWith("stub-")));
    }
}
