package com.financeapp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Aggregated analytics data returned by the dashboard API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

    /** Sum of all INCOME records (excluding soft-deleted) */
    private BigDecimal totalIncome;

    /** Sum of all EXPENSE records (excluding soft-deleted) */
    private BigDecimal totalExpenses;

    /** totalIncome - totalExpenses */
    private BigDecimal netBalance;

    /** Map of category → total amount */
    private Map<String, BigDecimal> categoryTotals;

    /** Last 10 transactions ordered by date desc */
    private List<FinancialRecordResponse> recentTransactions;

    /** Monthly income/expense breakdown: [{month, income, expenses}] */
    private List<MonthlyTrend> monthlyTrends;

    // -------------------------------------------------------
    // Nested projection for monthly data
    // -------------------------------------------------------

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyTrend {
        /** e.g. "2024-03" */
        private String month;
        private BigDecimal income;
        private BigDecimal expenses;
        private BigDecimal net;
    }
}
