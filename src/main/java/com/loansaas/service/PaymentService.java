package com.loansaas.service;

import com.loansaas.dto.PaymentDto;
import com.loansaas.entity.*;
import com.loansaas.repository.LoanRepository;
import com.loansaas.repository.PaymentRepository;
import com.loansaas.util.LoanCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final LoanRepository loanRepository;
    private final ActivityService activityService;

    public List<Payment> listForLender(Lender lender) {
        return paymentRepository.findByLenderOrderByPaymentDateDesc(lender);
    }

    public List<Payment> listForLoan(Loan loan) {
        return paymentRepository.findByLoanOrderByPaymentDateDesc(loan);
    }

    @Transactional
    public Payment record(PaymentDto dto, Lender lender, User actor) {
        Loan loan = loanRepository.findByIdAndLender(dto.getLoanId(), lender)
                .orElseThrow(() -> new IllegalArgumentException("Loan not found or access denied"));

        if (loan.getRemainingBalance().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("This loan is already fully paid.");
        }

        BigDecimal pay = dto.getAmountPaid();
        if (pay.compareTo(loan.getRemainingBalance()) > 0) {
            throw new IllegalArgumentException(
                    "Payment (" + pay + ") exceeds the remaining balance (" + loan.getRemainingBalance() + ").");
        }

        BigDecimal newPaid = loan.getAmountPaid().add(pay);
        BigDecimal newBalance = loan.getTotalRepayment().subtract(newPaid);

        loan.setAmountPaid(newPaid);
        loan.setRemainingBalance(newBalance);
        loan.setStatus(LoanCalculator.deriveStatus(loan));
        loanRepository.save(loan);

        Payment payment = Payment.builder()
                .loan(loan)
                .lender(lender)
                .amountPaid(pay)
                .paymentDate(dto.getPaymentDate())
                .balanceAfter(newBalance)
                .build();
        payment = paymentRepository.save(payment);

        activityService.log(actor.getId(), actor.getFullName(),
                "Recorded payment of " + pay + " TZS for loan #" + loan.getId()
                        + " (" + loan.getCustomer().getFullName() + ")");
        return payment;
    }
}
