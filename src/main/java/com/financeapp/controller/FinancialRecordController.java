package com.financeapp.controller;

import com.financeapp.dto.request.FinancialRecordRequest;
import com.financeapp.dto.response.FinancialRecordResponse;
import com.financeapp.model.enums.RecordType;
import com.financeapp.service.FinancialRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * REST controller for financial record CRUD + paginated search.
 *
 * Base path: /api/records
 *
 * Access rules (also enforced at route level in SecurityConfig):
 *  - POST / PUT / DELETE  →  ADMIN only
 *  - GET                  →  ANALYST, ADMIN
 */
@RestController
@RequestMapping("/records")
@RequiredArgsConstructor
@Tag(name = "Financial Records", description = "CRUD and search for financial records")
@SecurityRequirement(name = "bearerAuth")
public class FinancialRecordController {

    private final FinancialRecordService recordService;

    // -------------------------------------------------------
    // CREATE — ADMIN only
    // -------------------------------------------------------

    /**
     * POST /api/records
     * Create a new financial record. The creator is derived from the JWT token.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a financial record")
    public ResponseEntity<FinancialRecordResponse> createRecord(
            @Valid @RequestBody FinancialRecordRequest request,
            Authentication authentication          // Spring injects the current user
    ) {
        String creatorEmail = authentication.getName(); // getName() → email (our username)
        FinancialRecordResponse record = recordService.createRecord(request, creatorEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(record);
    }

    // -------------------------------------------------------
    // READ — ANALYST + ADMIN
    // -------------------------------------------------------

    /**
     * GET /api/records/{id}
     * Fetch a single record by ID.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    @Operation(summary = "Get a record by ID")
    public ResponseEntity<FinancialRecordResponse> getRecordById(@PathVariable Long id) {
        return ResponseEntity.ok(recordService.getRecordById(id));
    }

    /**
     * GET /api/records
     * Paginated, filtered list of records.
     *
     * Query params (all optional):
     *  - type      : INCOME | EXPENSE
     *  - category  : partial string match
     *  - from      : ISO date (yyyy-MM-dd)
     *  - to        : ISO date (yyyy-MM-dd)
     *  - page      : 0-indexed page number (default 0)
     *  - size      : page size (default 10, max 100)
     *  - sort      : field,direction e.g. date,desc
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    @Operation(summary = "List records (paginated + filtered)")
    public ResponseEntity<Page<FinancialRecordResponse>> getRecords(
            @Parameter(description = "Filter by INCOME or EXPENSE")
            @RequestParam(required = false) RecordType type,

            @Parameter(description = "Partial match on category name")
            @RequestParam(required = false) String category,

            @Parameter(description = "Start date inclusive (yyyy-MM-dd)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,

            @Parameter(description = "End date inclusive (yyyy-MM-dd)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,

            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        // Cap page size to prevent abuse
        int safeSize = Math.min(size, 100);

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Page<FinancialRecordResponse> records =
                recordService.getRecords(type, category, from, to,
                        PageRequest.of(page, safeSize, sort));
        return ResponseEntity.ok(records);
    }

    // -------------------------------------------------------
    // UPDATE — ADMIN only
    // -------------------------------------------------------

    /**
     * PUT /api/records/{id}
     * Replace all fields of an existing record.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a financial record (full replace)")
    public ResponseEntity<FinancialRecordResponse> updateRecord(
            @PathVariable Long id,
            @Valid @RequestBody FinancialRecordRequest request
    ) {
        return ResponseEntity.ok(recordService.updateRecord(id, request));
    }

    // -------------------------------------------------------
    // SOFT DELETE — ADMIN only
    // -------------------------------------------------------

    /**
     * DELETE /api/records/{id}
     * Soft-delete a record (sets deleted = true; data is preserved).
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Soft-delete a financial record",
               description = "Marks the record as deleted. Data is never physically removed.")
    public ResponseEntity<Void> deleteRecord(@PathVariable Long id) {
        recordService.deleteRecord(id);
        return ResponseEntity.noContent().build();      // 204 No Content
    }
}
