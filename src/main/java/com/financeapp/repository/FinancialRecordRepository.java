package com.financeapp.repository;

import com.financeapp.model.entity.FinancialRecord;
import com.financeapp.model.enums.RecordType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Data access layer for FinancialRecord entities.
 *
 * All queries automatically exclude soft-deleted records (deleted = false).
 * Custom JPQL is used for aggregation to avoid N+1 select problems.
 */
@Repository
public interface FinancialRecordRepository extends JpaRepository<FinancialRecord, Long> {

    // -------------------------------------------------------
    // Paginated listing with filters
    // -------------------------------------------------------

    /**
     * Retrieve non-deleted records with optional filters.
     * Any null parameter is treated as "no filter" via JPQL coalescing.
     */
    @Query("""
        SELECT r FROM FinancialRecord r
        WHERE r.deleted = false
          AND (:type     IS NULL OR r.type     = :type)
          AND (:category IS NULL OR LOWER(r.category) LIKE LOWER(CONCAT('%', :category, '%')))
          AND (:from     IS NULL OR r.date    >= :from)
          AND (:to       IS NULL OR r.date    <= :to)
        ORDER BY r.date DESC, r.createdAt DESC
        """)
    Page<FinancialRecord> findAllWithFilters(
            @Param("type")     RecordType type,
            @Param("category") String     category,
            @Param("from")     LocalDate  from,
            @Param("to")       LocalDate  to,
            Pageable pageable
    );

    // -------------------------------------------------------
    // Aggregation for dashboard
    // -------------------------------------------------------

    /**
     * Sum of all amounts for a given RecordType (e.g. total income, total expenses).
     * Returns 0 instead of null when no records exist.
     */
    @Query("""
        SELECT COALESCE(SUM(r.amount), 0)
        FROM FinancialRecord r
        WHERE r.deleted = false AND r.type = :type
        """)
    BigDecimal sumByType(@Param("type") RecordType type);

    /**
     * Category-wise totals for all non-deleted records.
     *
     * @return List of Object[]{category (String), total (BigDecimal)}
     */
    @Query("""
        SELECT r.category, COALESCE(SUM(r.amount), 0)
        FROM FinancialRecord r
        WHERE r.deleted = false
        GROUP BY r.category
        ORDER BY SUM(r.amount) DESC
        """)
    List<Object[]> categoryTotals();

    /**
     * Monthly aggregation grouped by year-month.
     * Returns income and expense totals per month.
     *
     * @return List of Object[]{yearMonth (String), type (String), total (BigDecimal)}
     */
    @Query(value = """
        SELECT DATE_FORMAT(date, '%Y-%m') AS month,
               type,
               COALESCE(SUM(amount), 0)  AS total
        FROM financial_records
        WHERE deleted = false
        GROUP BY month, type
        ORDER BY month ASC
        """, nativeQuery = true)
    List<Object[]> monthlyTrends();

    /**
     * Recent non-deleted transactions (default: last 10).
     */
    @Query("""
        SELECT r FROM FinancialRecord r
        WHERE r.deleted = false
        ORDER BY r.date DESC, r.createdAt DESC
        """)
    List<FinancialRecord> findRecentTransactions(Pageable pageable);
}
