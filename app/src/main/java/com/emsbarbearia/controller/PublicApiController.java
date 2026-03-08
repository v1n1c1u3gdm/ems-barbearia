package com.emsbarbearia.controller;

import com.emsbarbearia.dto.AgendamentoRequest;
import com.emsbarbearia.dto.AgendamentoResponse;
import com.emsbarbearia.dto.ServicoResponse;
import com.emsbarbearia.dto.StaffResponse;
import com.emsbarbearia.service.AgendamentoService;
import com.emsbarbearia.service.ServicoService;
import com.emsbarbearia.service.StaffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Public API", description = "Endpoints for public booking (no auth)")
public class PublicApiController {

    private final ServicoService servicoService;
    private final StaffService staffService;
    private final AgendamentoService agendamentoService;

    public PublicApiController(ServicoService servicoService, StaffService staffService, AgendamentoService agendamentoService) {
        this.servicoService = servicoService;
        this.staffService = staffService;
        this.agendamentoService = agendamentoService;
    }

    @GetMapping("/servicos")
    @Operation(summary = "List active servicos for booking form")
    public List<ServicoResponse> listServicos() {
        return servicoService.listAtivos();
    }

    @GetMapping("/staff")
    @Operation(summary = "List active staff for booking form")
    public List<StaffResponse> listStaff() {
        return staffService.listAtivos();
    }

    @PostMapping("/agendamentos")
    @Operation(summary = "Create agendamento (status PENDENTE)")
    public ResponseEntity<AgendamentoResponse> createAgendamento(@Valid @RequestBody AgendamentoRequest request) {
        AgendamentoRequest publicRequest = new AgendamentoRequest(
            request.clienteId(),
            request.servicoId(),
            request.staffId(),
            request.dataHora(),
            request.tipo(),
            null
        );
        return agendamentoService.create(publicRequest)
            .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body))
            .orElse(ResponseEntity.notFound().build());
    }
}
