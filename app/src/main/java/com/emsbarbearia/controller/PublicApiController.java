package com.emsbarbearia.controller;

import com.emsbarbearia.dto.AgendamentoRequest;
import com.emsbarbearia.dto.AgendamentoResponse;
import com.emsbarbearia.dto.ProverbioResponse;
import com.emsbarbearia.dto.PublicAgendamentoRequest;
import com.emsbarbearia.dto.ServicoResponse;
import com.emsbarbearia.dto.StaffResponse;
import com.emsbarbearia.service.AgendamentoService;
import com.emsbarbearia.service.ProverbioService;
import com.emsbarbearia.service.ServicoService;
import com.emsbarbearia.service.StaffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("")
@Tag(name = "Public API", description = "Endpoints for public booking")
public class PublicApiController {

    private final ServicoService servicoService;
    private final StaffService staffService;
    private final AgendamentoService agendamentoService;
    private final ProverbioService proverbioService;

    public PublicApiController(ServicoService servicoService, StaffService staffService,
                               AgendamentoService agendamentoService, ProverbioService proverbioService) {
        this.servicoService = servicoService;
        this.staffService = staffService;
        this.agendamentoService = agendamentoService;
        this.proverbioService = proverbioService;
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

    @GetMapping("/proverbios/random")
    @Operation(summary = "Return a random biblical proverb")
    public ResponseEntity<ProverbioResponse> getProverbioRandom() {
        return proverbioService.getRandom()
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/agendamentos")
    @Operation(summary = "Create agendamento (status PENDENTE); requires Bearer JWT, cliente from token")
    public ResponseEntity<AgendamentoResponse> createAgendamento(
            @Valid @RequestBody PublicAgendamentoRequest request,
            @AuthenticationPrincipal Long clienteId) {
        if (clienteId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        AgendamentoRequest fullRequest = new AgendamentoRequest(
            clienteId,
            request.servicoId(),
            request.staffId(),
            request.dataHora(),
            request.tipo(),
            null
        );
        return agendamentoService.create(fullRequest)
            .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body))
            .orElse(ResponseEntity.notFound().build());
    }
}
