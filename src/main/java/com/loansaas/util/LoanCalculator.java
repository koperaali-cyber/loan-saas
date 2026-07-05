package com.loansaas.util;

import com.loansaas.entity.Loan;
import com.loansaas.entity.LoanStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public final class LoanCalculator {

    private LoanCalculator() {}

    /**
     * Simple interest model:
     * totalRepayment = principal + (principal * rate% )
     * Rate is a flat percentage over the whole loan term (easy to understand).
     */
    public static BigDecimal totalRepayment(BigDecimal amount, BigDecimal ratePercent) {
        BigDecimal interest = amount.multiply(ratePercent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return amount.add(interest).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Derive loan status from balances and due date.
     */
    public static LoanStatus deriveStatus(Loan loan) {
        BigDecimal remaining = loan.getRemainingBalance();
        BigDecimal paid = loan.getAmountPaid() == null ? BigDecimal.ZERO : loan.getAmountPaid();
        LocalDate today = LocalDate.now();

        if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
            return LoanStatus.PAID;
        }
        boolean overdue = loan.getDueDate() != null && today.isAfter(loan.getDueDate());
        if (overdue) {
            // Overdue with nothing left to pay wouldn't reach here; still owing after due date
            return LoanStatus.OVERDUE;
        }
        if (paid.compareTo(BigDecimal.ZERO) > 0) {
            return LoanStatus.PARTIALLY_PAID;
        }
        return LoanStatus.ACTIVE;
    }
}
