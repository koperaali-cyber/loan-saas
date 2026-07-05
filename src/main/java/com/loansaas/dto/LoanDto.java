package com.loansaas.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class LoanDto {

    private Long id;

    @NotNull(message = "Select a customer")
    private Long customerId;

    @NotNull(message = "Loan amount is required")
    @DecimalMin(value = "1000", message = "Amount must be at least 1,000 TZS")
    private BigDecimal amount;

    @NotNull(message = "Interest rate is required")
    @DecimalMin(value = "0", message = "Interest rate cannot be negative")
    private BigDecimal interestRate;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 month")
    private Integer duration;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private LocalDate dueDate;
}
