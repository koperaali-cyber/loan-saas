package com.loansaas.repository;

import com.loansaas.entity.Lender;
import com.loansaas.entity.Loan;
import com.loansaas.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByLenderOrderByPaymentDateDesc(Lender lender);
    List<Payment> findByLoanOrderByPaymentDateDesc(Loan loan);
    long countByLender(Lender lender);
}
