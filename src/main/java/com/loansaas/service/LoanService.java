package com.loansaas.service;

import com.loansaas.dto.LoanDto;
import com.loansaas.entity.*;
import com.loansaas.repository.LoanRepository;
import com.loansaas.util.LoanCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepository;
    private final CustomerService customerService;
    private final ActivityService activityService;

    public List<Loan> listForLender(Lender lender) {
        List<Loan> loans = loanRepository.findByLenderOrderByCreatedAtDesc(lender);
        loans.forEach(this::refreshOverdue);
        return loans;
    }

    public List<Loan> listForCustomer(Customer customer) {
        return loanRepository.findByCustomerOrderByCreatedAtDesc(customer);
    }

    public Loan getForLender(Long id, Lender lender) {
        Loan loan = loanRepository.findByIdAndLender(id, lender)
                .orElseThrow(() -> new IllegalArgumentException("Loan not found or access denied"));
        refreshOverdue(loan);
        return loan;
    }

    @Transactional
    public Loan create(LoanDto dto, Lender lender, User actor) {
        Customer customer = customerService.getForLender(dto.getCustomerId(), lender);

        LocalDate start = dto.getStartDate();
        LocalDate due = dto.getDueDate() != null
                ? dto.getDueDate()
                : start.plusMonths(dto.getDuration());

        BigDecimal total = LoanCalculator.totalRepayment(dto.getAmount(), dto.getInterestRate());

        Loan loan = Loan.builder()
                .customer(customer)
                .lender(lender)
                .amount(dto.getAmount())
                .interestRate(dto.getInterestRate())
                .duration(dto.getDuration())
                .startDate(start)
                .dueDate(due)
                .totalRepayment(total)
                .amountPaid(BigDecimal.ZERO)
                .remainingBalance(total)
                .status(LoanStatus.ACTIVE)
                .build();

        loan.setStatus(LoanCalculator.deriveStatus(loan));
        loan = loanRepository.save(loan);

        activityService.log(actor.getId(), actor.getFullName(),
                "Issued loan of " + dto.getAmount() + " TZS to " + customer.getFullName());
        return loan;
    }

    @Transactional
    public void markDefaulted(Long id, Lender lender, User actor) {
        Loan loan = getForLender(id, lender);
        loan.setStatus(LoanStatus.DEFAULTED);
        loanRepository.save(loan);
        activityService.log(actor.getId(), actor.getFullName(),
                "Marked loan #" + id + " as DEFAULTED");
    }

    @Transactional
    public void delete(Long id, Lender lender, User actor) {
        Loan loan = getForLender(id, lender);
        loanRepository.delete(loan);
        activityService.log(actor.getId(), actor.getFullName(), "Deleted loan #" + id);
    }

    /** Recompute OVERDUE status lazily on read. */
    @Transactional
    public void refreshOverdue(Loan loan) {
        if (loan.getStatus() == LoanStatus.PAID || loan.getStatus() == LoanStatus.DEFAULTED) {
            return;
        }
        boolean stillOwing = loan.getRemainingBalance().compareTo(BigDecimal.ZERO) > 0;
        boolean pastDue = loan.getDueDate() != null && LocalDate.now().isAfter(loan.getDueDate());
        LoanStatus newStatus;
        if (!stillOwing) {
            newStatus = LoanStatus.PAID;
        } else if (pastDue) {
            newStatus = LoanStatus.OVERDUE;
        } else if (loan.getAmountPaid() != null
                && loan.getAmountPaid().compareTo(BigDecimal.ZERO) > 0) {
            newStatus = LoanStatus.PARTIALLY_PAID;
        } else {
            newStatus = LoanStatus.ACTIVE;
        }
        if (newStatus != loan.getStatus()) {
            loan.setStatus(newStatus);
            loanRepository.save(loan);
        }
    }
}
