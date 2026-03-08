package com.emsbarbearia.controller;

import com.emsbarbearia.dto.AgendamentoRequest;
import com.emsbarbearia.dto.AgendamentoResponse;
import com.emsbarbearia.service.AgendamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/agendamentos")
@Tag(name = "Admin - Agendamentos", description = "CRUD for agendamentos (admin)")
public class AgendamentoController {

    private final AgendamentoService service;

    public AgendamentoController(AgendamentoService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List all agendamentos")
    public List<AgendamentoResponse> list(@RequestParam(required = false) Long clienteId) {
        return service.list(clienteId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get agendamento by id")
    public ResponseEntity<AgendamentoResponse> getById(@PathVariable Long id) {
        return service.getById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create an agendamento")
    public ResponseEntity<AgendamentoResponse> create(@Valid @RequestBody AgendamentoRequest request) {
        return service.create(request)
            .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body))
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an agendamento")
    public ResponseEntity<AgendamentoResponse> update(@PathVariable Long id, @Valid @RequestBody AgendamentoRequest request) {
        return service.update(id, request)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an agendamento")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!service.delete(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
