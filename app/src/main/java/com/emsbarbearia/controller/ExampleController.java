package com.emsbarbearia.controller;

import com.emsbarbearia.dto.ExampleRequest;
import com.emsbarbearia.dto.ExampleResponse;
import com.emsbarbearia.entity.ExampleEntity;
import com.emsbarbearia.repository.ExampleRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/examples")
@Tag(name = "Examples", description = "Example CRUD API")
public class ExampleController {

    private final ExampleRepository repository;

    public ExampleController(ExampleRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    @Operation(summary = "List all examples")
    public List<ExampleResponse> list(@RequestParam(required = false) String name) {
        List<ExampleEntity> entities = name != null && !name.isBlank()
            ? repository.findByNameContainingIgnoreCase(name)
            : repository.findAll();
        return entities.stream()
            .map(this::toResponse)
            .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get example by id")
    public ResponseEntity<ExampleResponse> getById(@PathVariable Long id) {
        return repository.findById(id)
            .map(this::toResponse)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create a new example")
    public ResponseEntity<ExampleResponse> create(@Valid @RequestBody ExampleRequest request) {
        ExampleEntity entity = new ExampleEntity();
        entity.setName(request.name());
        ExampleEntity saved = repository.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an example")
    public ResponseEntity<ExampleResponse> update(@PathVariable Long id, @Valid @RequestBody ExampleRequest request) {
        return repository.findById(id)
            .map(entity -> {
                entity.setName(request.name());
                return repository.save(entity);
            })
            .map(this::toResponse)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an example")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private ExampleResponse toResponse(ExampleEntity entity) {
        return new ExampleResponse(
            entity.getId(),
            entity.getName(),
            entity.getCreatedAt()
        );
    }
}
