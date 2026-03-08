package com.emsbarbearia.controller;

import com.emsbarbearia.dto.DashboardSummaryResponse;
import com.emsbarbearia.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/dashboard")
@Tag(name = "Admin - Dashboard", description = "Dashboard summary (admin)")
public class DashboardController {

    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    @GetMapping("/summary")
    @Operation(summary = "Get dashboard summary counts")
    public DashboardSummaryResponse getSummary() {
        return service.getSummary();
    }
}
