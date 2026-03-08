package com.emsbarbearia.controller;

import com.emsbarbearia.dto.PromocaoRequest;
import com.emsbarbearia.dto.PromocaoResponse;
import com.emsbarbearia.service.PromocaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/promocoes")
@Tag(name = "Admin - Promocoes", description = "CRUD for promocoes (admin)")
public class PromocaoController {

    private final PromocaoService service;

    public PromocaoController(PromocaoService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List all promocoes")
    public List<PromocaoResponse> list(@RequestParam(required = false) String titulo) {
        return service.list(titulo);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get promocao by id")
    public ResponseEntity<PromocaoResponse> getById(@PathVariable Long id) {
        return service.getById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create a promocao")
    public ResponseEntity<PromocaoResponse> create(@Valid @RequestBody PromocaoRequest request) {
        PromocaoResponse body = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a promocao")
    public ResponseEntity<PromocaoResponse> update(@PathVariable Long id, @Valid @RequestBody PromocaoRequest request) {
        return service.update(id, request)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a promocao")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!service.delete(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
