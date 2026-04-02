package com.financeapp.service.impl;

import com.financeapp.dto.response.DashboardResponse;
import com.financeapp.dto.response.FinancialRecordResponse;
import com.financeapp.model.enums.RecordType;
import com.financeapp.repository.FinancialRecordRepository;
import com.financeapp.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of {@link DashboardService}.
 *
 * Aggregation strategy:
 *  - Total income/expenses: single-query SUM via JPQL (indexed on type).
 *  - Category totals: GROUP BY query — returns a compact result set.
 *  - Monthly trends: native MySQL DATE_FORMAT GROUP BY for readability.
 *  - Recent transactions: limited fetch (10 rows) with date-desc ordering.
 *
 * All reads are read-only transactions — Hibernate skips dirty checking.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final FinancialRecordRepository recordRepository;

    private static final int RECENT_TRANSACTIONS_LIMIT = 10;

    @Override
    @Transactional(readOnly = true)
    public DashboardResponse getDashboardData() {
        // 1. Aggregate totals
        BigDecimal totalIncome   = recordRepository.sumByType(RecordType.INCOME);
        BigDecimal totalExpenses = recordRepository.sumByType(RecordType.EXPENSE);
        BigDecimal netBalance    = totalIncome.subtract(totalExpenses);

        // 2. Category-wise totals
        Map<String, BigDecimal> categoryTotals = buildCategoryTotals();

        // 3. Recent 10 transactions
        List<FinancialRecordResponse> recentTransactions = recordRepository
                .findRecentTransactions(PageRequest.of(0, RECENT_TRANSACTIONS_LIMIT))
                .stream()
                .map(r -> FinancialRecordResponse.builder()
                        .id(r.getId())
                        .amount(r.getAmount())
                        .type(r.getType())
                        .category(r.getCategory())
                        .date(r.getDate())
                        .notes(r.getNotes())
                        .createdByName(r.getCreatedBy().getName())
                        .createdByEmail(r.getCreatedBy().getEmail())
                        .createdAt(r.getCreatedAt())
                        .updatedAt(r.getUpdatedAt())
                        .build())
                .toList();

        // 4. Monthly trends
        List<DashboardResponse.MonthlyTrend> monthlyTrends = buildMonthlyTrends();

        log.debug("Dashboard computed — income={}, expenses={}, net={}",
                totalIncome, totalExpenses, netBalance);

        return DashboardResponse.builder()
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .netBalance(netBalance)
                .categoryTotals(categoryTotals)
                .recentTransactions(recentTransactions)
                .monthlyTrends(monthlyTrends)
                .build();
    }

    // -------------------------------------------------------
    // Private aggregation helpers
    // -------------------------------------------------------

    /**
     * Build category→total map from the repository projection.
     * Object[0] = category (String), Object[1] = total (BigDecimal).
     */
    private Map<String, BigDecimal> buildCategoryTotals() {
        return recordRepository.categoryTotals()
                .stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> new BigDecimal(row[1].toString()),
                        BigDecimal::add,                     // merge duplicate categories (defensive)
                        LinkedHashMap::new                   // preserve insertion order (sorted by total desc)
                ));
    }

    /**
     * Pivot the raw [month, type, total] rows into MonthlyTrend objects.
     *
     * Raw row: Object[0]=month(String), Object[1]=type(String), Object[2]=total(BigDecimal)
     *
     * We merge INCOME and EXPENSE rows for the same month into one trend object.
     */
    private List<DashboardResponse.MonthlyTrend> buildMonthlyTrends() {
        List<Object[]> rows = recordRepository.monthlyTrends();

        // Accumulate per-month data using a LinkedHashMap to preserve month order
        Map<String, DashboardResponse.MonthlyTrend> trendMap = new LinkedHashMap<>();

        for (Object[] row : rows) {
            String month    = (String)     row[0];
            String type     = (String)     row[1];
            BigDecimal total = new BigDecimal(row[2].toString());

            trendMap.computeIfAbsent(month, m -> DashboardResponse.MonthlyTrend.builder()
                    .month(m)
                    .income(BigDecimal.ZERO)
                    .expenses(BigDecimal.ZERO)
                    .net(BigDecimal.ZERO)
                    .build());

            DashboardResponse.MonthlyTrend trend = trendMap.get(month);

            if (RecordType.INCOME.name().equalsIgnoreCase(type)) {
                trend.setIncome(total);
            } else if (RecordType.EXPENSE.name().equalsIgnoreCase(type)) {
                trend.setExpenses(total);
            }

            // Recalculate net after each update
            trend.setNet(trend.getIncome().subtract(trend.getExpenses()));
        }

        return new ArrayList<>(trendMap.values());
    }
}
