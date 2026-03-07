package com.emsbarbearia.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.emsbarbearia.entity.ExampleEntity;
import com.emsbarbearia.repository.ExampleRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ExampleController.class)
class ExampleControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    ExampleRepository repository;

    @Test
    void list_shouldReturn200AndAllWhenNoFilter() throws Exception {
        var entity = entity(1L, "Foo");
        when(repository.findAll()).thenReturn(List.of(entity));

        mockMvc.perform(get("/examples"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].name").value("Foo"));
    }

    @Test
    void list_shouldFilterByNameWhenNameParamGiven() throws Exception {
        var entity = entity(2L, "Bar");
        when(repository.findByNameContainingIgnoreCase("bar")).thenReturn(List.of(entity));

        mockMvc.perform(get("/examples").param("name", "bar"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].name").value("Bar"));
    }

    @Test
    void getById_shouldReturn200WhenFound() throws Exception {
        var entity = entity(1L, "One");
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        mockMvc.perform(get("/examples/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("One"));
    }

    @Test
    void getById_shouldReturn404WhenNotFound() throws Exception {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/examples/999"))
            .andExpect(status().isNotFound());
    }

    @Test
    void create_shouldReturn201WithBody() throws Exception {
        var entity = entity(1L, "New");
        when(repository.save(any(ExampleEntity.class))).thenReturn(entity);

        mockMvc.perform(post("/examples")
                .contentType(APPLICATION_JSON)
                .content("{\"name\":\"New\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("New"));
    }

    @Test
    void create_shouldReturn400WhenNameBlank() throws Exception {
        mockMvc.perform(post("/examples")
                .contentType(APPLICATION_JSON)
                .content("{\"name\":\"\"}"))
            .andExpect(status().isBadRequest());
        verify(repository, never()).save(any());
    }

    @Test
    void update_shouldReturn200WhenFound() throws Exception {
        var entity = entity(1L, "Updated");
        when(repository.findById(1L)).thenReturn(Optional.of(entity(1L, "Old")));
        when(repository.save(any(ExampleEntity.class))).thenReturn(entity);

        mockMvc.perform(put("/examples/1")
                .contentType(APPLICATION_JSON)
                .content("{\"name\":\"Updated\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    void update_shouldReturn404WhenNotFound() throws Exception {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/examples/999")
                .contentType(APPLICATION_JSON)
                .content("{\"name\":\"Any\"}"))
            .andExpect(status().isNotFound());
        verify(repository, never()).save(any());
    }

    @Test
    void delete_shouldReturn204WhenExists() throws Exception {
        when(repository.existsById(1L)).thenReturn(true);

        mockMvc.perform(delete("/examples/1"))
            .andExpect(status().isNoContent());
        verify(repository).deleteById(1L);
    }

    @Test
    void delete_shouldReturn404WhenNotExists() throws Exception {
        when(repository.existsById(999L)).thenReturn(false);

        mockMvc.perform(delete("/examples/999"))
            .andExpect(status().isNotFound());
        verify(repository, never()).deleteById(eq(999L));
    }

    private static ExampleEntity entity(Long id, String name) {
        var e = new ExampleEntity();
        e.setId(id);
        e.setName(name);
        e.setCreatedAt(Instant.now());
        return e;
    }
}
