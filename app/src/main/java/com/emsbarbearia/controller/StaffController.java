package com.emsbarbearia.controller;

import com.emsbarbearia.dto.StaffRequest;
import com.emsbarbearia.dto.StaffResponse;
import com.emsbarbearia.service.StaffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/admin/staff")
@Tag(name = "Admin - Staff", description = "CRUD for staff (admin)")
public class StaffController {

    private final StaffService service;

    public StaffController(StaffService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List all staff")
    public List<StaffResponse> list(@RequestParam(required = false) String nome) {
        return service.list(nome);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get staff by id")
    public ResponseEntity<StaffResponse> getById(@PathVariable Long id) {
        return service.getById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create staff")
    public ResponseEntity<StaffResponse> create(@Valid @RequestBody StaffRequest request) {
        StaffResponse body = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update staff")
    public ResponseEntity<StaffResponse> update(@PathVariable Long id, @Valid @RequestBody StaffRequest request) {
        return service.update(id, request)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete staff")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!service.delete(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
