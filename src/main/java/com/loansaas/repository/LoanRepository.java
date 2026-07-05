package com.loansaas.repository;

import com.loansaas.entity.Customer;
import com.loansaas.entity.Lender;
import com.loansaas.entity.Loan;
import com.loansaas.entity.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByLenderOrderByCreatedAtDesc(Lender lender);
    List<Loan> findByCustomerOrderByCreatedAtDesc(Customer customer);
    Optional<Loan> findByIdAndLender(Long id, Lender lender);
    long countByLender(Lender lender);
    long countByLenderAndStatus(Lender lender, LoanStatus status);
    long countByCustomer(Customer customer);
    long countByCustomerAndStatus(Customer customer, LoanStatus status);
    List<Loan> findByStatusIn(List<LoanStatus> statuses);

    @Query("SELECT COALESCE(SUM(l.amount),0) FROM Loan l WHERE l.lender = :lender")
    BigDecimal totalIssuedByLender(@Param("lender") Lender lender);

    @Query("SELECT COALESCE(SUM(l.amountPaid),0) FROM Loan l WHERE l.lender = :lender")
    BigDecimal totalCollectedByLender(@Param("lender") Lender lender);

    @Query("SELECT COALESCE(SUM(l.amount),0) FROM Loan l")
    BigDecimal totalIssuedAll();

    @Query("SELECT COALESCE(SUM(l.amountPaid),0) FROM Loan l")
    BigDecimal totalCollectedAll();
}
