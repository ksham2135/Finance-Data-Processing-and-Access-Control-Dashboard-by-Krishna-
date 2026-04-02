package com.financeapp.service;

import com.financeapp.dto.request.FinancialRecordRequest;
import com.financeapp.dto.response.FinancialRecordResponse;
import com.financeapp.model.enums.RecordType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

/**
 * Contract for financial record CRUD and search operations.
 */
public interface FinancialRecordService {

    FinancialRecordResponse createRecord(FinancialRecordRequest request, String creatorEmail);

    FinancialRecordResponse updateRecord(Long id, FinancialRecordRequest request);

    void deleteRecord(Long id);

    FinancialRecordResponse getRecordById(Long id);

    /**
     * Returns a paginated, filtered list of non-deleted records.
     *
     * @param type     optional filter by INCOME/EXPENSE
     * @param category optional partial-match on category
     * @param from     optional start date (inclusive)
     * @param to       optional end date (inclusive)
     * @param pageable pagination + sort
     */
    Page<FinancialRecordResponse> getRecords(
            RecordType type,
            String     category,
            LocalDate  from,
            LocalDate  to,
            Pageable   pageable
    );
}
