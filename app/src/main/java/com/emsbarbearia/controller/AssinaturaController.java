package com.emsbarbearia.controller;

import com.emsbarbearia.dto.AssinaturaRequest;
import com.emsbarbearia.dto.AssinaturaResponse;
import com.emsbarbearia.service.AssinaturaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/assinaturas")
@Tag(name = "Admin - Assinaturas", description = "CRUD for assinaturas (one client, one or more servicos)")
public class AssinaturaController {

    private final AssinaturaService service;

    public AssinaturaController(AssinaturaService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List assinaturas with optional cliente filter")
    public List<AssinaturaResponse> list(@RequestParam(required = false) Long clienteId) {
        return service.list(clienteId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get assinatura by id")
    public ResponseEntity<AssinaturaResponse> getById(@PathVariable Long id) {
        return service.getById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create an assinatura")
    public ResponseEntity<AssinaturaResponse> create(@Valid @RequestBody AssinaturaRequest request) {
        return service.create(request)
            .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body))
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an assinatura")
    public ResponseEntity<AssinaturaResponse> update(@PathVariable Long id, @Valid @RequestBody AssinaturaRequest request) {
        return service.update(id, request)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an assinatura")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!service.delete(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
