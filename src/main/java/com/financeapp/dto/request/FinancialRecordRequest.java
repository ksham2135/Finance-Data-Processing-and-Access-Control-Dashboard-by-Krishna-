package com.financeapp.dto.request;

import com.financeapp.model.enums.RecordType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request DTO for creating or updating a financial record.
 */
@Data
public class FinancialRecordRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 15, fraction = 4, message = "Amount format invalid")
    private BigDecimal amount;

    @NotNull(message = "Type is required (INCOME or EXPENSE)")
    private RecordType type;

    @NotBlank(message = "Category is required")
    @Size(max = 100, message = "Category must not exceed 100 characters")
    private String category;

    @NotNull(message = "Date is required")
    @PastOrPresent(message = "Date must not be in the future")
    private LocalDate date;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;
}
