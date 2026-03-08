package com.emsbarbearia.controller;

import com.emsbarbearia.dto.RelacionamentoRequest;
import com.emsbarbearia.dto.RelacionamentoResponse;
import com.emsbarbearia.dto.RelacionamentoUpdateRequest;
import com.emsbarbearia.entity.CanalRelacionamento;
import com.emsbarbearia.entity.StatusRelacionamento;
import com.emsbarbearia.service.RelacionamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/relacionamentos")
@Tag(name = "Admin - Relacionamentos", description = "CRUD and list by canal/status (admin)")
public class RelacionamentoController {

    private final RelacionamentoService service;

    public RelacionamentoController(RelacionamentoService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List relacionamentos with optional canal and status filters")
    public List<RelacionamentoResponse> list(
        @RequestParam(required = false) String canal,
        @RequestParam(required = false) String status) {
        CanalRelacionamento canalEnum = parseCanal(canal);
        StatusRelacionamento statusEnum = parseStatus(status);
        return service.list(canalEnum, statusEnum);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get relacionamento by id")
    public ResponseEntity<RelacionamentoResponse> getById(@PathVariable Long id) {
        return service.getById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create a relacionamento")
    public ResponseEntity<RelacionamentoResponse> create(@Valid @RequestBody RelacionamentoRequest request) {
        RelacionamentoResponse body = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update relacionamento (status, dataUltimaInteracao, tipoInteracao)")
    public ResponseEntity<RelacionamentoResponse> update(
        @PathVariable Long id,
        @RequestBody RelacionamentoUpdateRequest request) {
        if (request == null) {
            return ResponseEntity.badRequest().build();
        }
        return service.update(id, request)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a relacionamento")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!service.delete(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    private static CanalRelacionamento parseCanal(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return CanalRelacionamento.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static StatusRelacionamento parseStatus(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return StatusRelacionamento.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
