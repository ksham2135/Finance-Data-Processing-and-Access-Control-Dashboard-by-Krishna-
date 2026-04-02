package com.financeapp.service;

import com.financeapp.dto.response.DashboardResponse;

/**
 * Contract for dashboard analytics aggregation.
 */
public interface DashboardService {

    /**
     * Returns aggregated financial analytics:
     * income, expenses, net balance, category totals,
     * recent transactions, and monthly trends.
     */
    DashboardResponse getDashboardData();
}
