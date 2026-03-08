package com.emsbarbearia.controller;

import com.emsbarbearia.dto.ServicoRequest;
import com.emsbarbearia.dto.ServicoResponse;
import com.emsbarbearia.service.ServicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/servicos")
@Tag(name = "Admin - Servicos", description = "CRUD for servicos (admin)")
public class ServicoController {

    private final ServicoService service;

    public ServicoController(ServicoService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List all servicos")
    public List<ServicoResponse> list(@RequestParam(required = false) String titulo) {
        return service.list(titulo);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get servico by id")
    public ResponseEntity<ServicoResponse> getById(@PathVariable Long id) {
        return service.getById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create a servico")
    public ResponseEntity<ServicoResponse> create(@Valid @RequestBody ServicoRequest request) {
        ServicoResponse body = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a servico")
    public ResponseEntity<ServicoResponse> update(@PathVariable Long id, @Valid @RequestBody ServicoRequest request) {
        return service.update(id, request)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a servico")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!service.delete(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
