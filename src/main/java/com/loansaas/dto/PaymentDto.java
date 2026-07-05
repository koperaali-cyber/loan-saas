package com.loansaas.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class PaymentDto {

    @NotNull(message = "Select a loan")
    private Long loanId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1", message = "Amount must be greater than zero")
    private BigDecimal amountPaid;

    @NotNull(message = "Payment date is required")
    private LocalDate paymentDate;
}
