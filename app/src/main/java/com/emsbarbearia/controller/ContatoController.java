package com.emsbarbearia.controller;

import com.emsbarbearia.dto.ContatoRequest;
import com.emsbarbearia.dto.ContatoResponse;
import com.emsbarbearia.service.ContatoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/contatos")
@Tag(name = "Admin - Contatos", description = "CRUD for contatos (admin)")
public class ContatoController {

    private final ContatoService service;

    public ContatoController(ContatoService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List all contatos")
    public List<ContatoResponse> list(@RequestParam(required = false) String nome) {
        return service.list(nome);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get contato by id")
    public ResponseEntity<ContatoResponse> getById(@PathVariable Long id) {
        return service.getById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create a contato")
    public ResponseEntity<ContatoResponse> create(@Valid @RequestBody ContatoRequest request) {
        ContatoResponse body = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a contato")
    public ResponseEntity<ContatoResponse> update(@PathVariable Long id, @Valid @RequestBody ContatoRequest request) {
        return service.update(id, request)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a contato")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!service.delete(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
