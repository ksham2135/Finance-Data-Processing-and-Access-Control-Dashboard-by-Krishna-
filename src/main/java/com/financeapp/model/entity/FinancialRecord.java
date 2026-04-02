package com.financeapp.model.entity;

import com.financeapp.model.enums.RecordType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * JPA entity representing a financial transaction record.
 *
 * Table: financial_records
 * Uses DECIMAL(19,4) for monetary precision (avoids floating-point drift).
 * Supports soft delete via `deleted` flag.
 */
@Entity
@Table(
    name = "financial_records",
    indexes = {
        @Index(name = "idx_fr_type",       columnList = "type"),
        @Index(name = "idx_fr_category",   columnList = "category"),
        @Index(name = "idx_fr_date",       columnList = "date"),
        @Index(name = "idx_fr_created_by", columnList = "created_by"),
        @Index(name = "idx_fr_deleted",    columnList = "deleted")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Monetary amount. Uses DECIMAL(19,4) for precision.
     * Must be positive — type (INCOME/EXPENSE) conveys sign intent.
     */
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private RecordType type;

    @Column(nullable = false, length = 100)
    private String category;

    /** Business date of the transaction (not the audit timestamp) */
    @Column(nullable = false)
    private LocalDate date;

    @Column(length = 500)
    private String notes;

    /**
     * The user who created this record.
     * LAZY loading — only fetched via explicit join queries.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    /** Soft-delete flag — records are never physically removed */
    @Column(nullable = false)
    @Builder.Default
    private boolean deleted = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // -------------------------------------------------------
    // Lifecycle hooks
    // -------------------------------------------------------

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
