package com.financeapp.controller;

import com.financeapp.dto.response.DashboardResponse;
import com.financeapp.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for the analytics/dashboard endpoint.
 *
 * Base path: /api/dashboard
 * Access: VIEWER, ANALYST, ADMIN (all authenticated users)
 */
@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard & Analytics", description = "Aggregated financial analytics endpoints")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * GET /api/dashboard
     * Returns the full analytics payload:
     *  - Total income, expenses, net balance
     *  - Category-wise totals
     *  - Recent 10 transactions
     *  - Monthly income/expense trends
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('VIEWER', 'ANALYST', 'ADMIN')")
    @Operation(
        summary = "Get dashboard analytics",
        description = """
            Returns aggregated financial data:
            - Total income and expenses
            - Net balance (income - expenses)
            - Category-wise breakdown
            - Last 10 transactions
            - Month-by-month income/expense trends
            """
    )
    public ResponseEntity<DashboardResponse> getDashboard() {
        return ResponseEntity.ok(dashboardService.getDashboardData());
    }
}
