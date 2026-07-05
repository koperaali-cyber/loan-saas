package com.loansaas.config;

import com.loansaas.entity.*;
import com.loansaas.repository.*;
import com.loansaas.util.LoanCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final LenderRepository lenderRepository;
    private final CustomerRepository customerRepository;
    private final LoanRepository loanRepository;
    private final PaymentRepository paymentRepository;
    private final PasswordEncoder encoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return; // already seeded
        }

        // ---- Super Admin ----
        User admin = User.builder()
                .fullName("System Administrator")
                .email("admin@loansaas.co.tz")
                .phone("+255700000000")
                .password(encoder.encode("admin123"))
                .role(Role.SUPER_ADMIN)
                .status(UserStatus.ACTIVE)
                .build();
        userRepository.save(admin);

        // ---- Lender A (approved) ----
        Lender lenderA = createLender("Juma Mwakasege", "Mwakasege Microfinance",
                "+255712345678", "juma@mwakasege.co.tz", UserStatus.APPROVED);

        // ---- Lender B (approved) ----
        Lender lenderB = createLender("Neema Kimaro", "Kilimanjaro Credit Ltd",
                "+255754112233", "neema@kilicredit.co.tz", UserStatus.APPROVED);

        // ---- Lender C (pending approval) ----
        createLender("Baraka Shirima", "Arusha Quick Loans",
                "+255786998877", "baraka@arushaloans.co.tz", UserStatus.PENDING_APPROVAL);

        // ---- Customers for Lender A ----
        Customer c1 = createCustomer(lenderA, "Amina Hassan", "+255715000001",
                "+255715000011", "19900101-12345-00001-01");
        Customer c2 = createCustomer(lenderA, "Rajabu Mfaume", "+255715000002",
                null, "19850315-22345-00002-02");
        Customer c3 = createCustomer(lenderA, "Grace Mushi", "+255715000003",
                "+255715000033", "19921120-32345-00003-03");

        // ---- Customers for Lender B ----
        Customer c4 = createCustomer(lenderB, "Emmanuel Laizer", "+255715000004",
                null, "19880708-42345-00004-04");
        Customer c5 = createCustomer(lenderB, "Fatuma Ally", "+255715000005",
                "+255715000055", "19950922-52345-00005-05");

        // ---- Loans + payments for Lender A ----
        Loan l1 = createLoan(lenderA, c1, "500000", "10", 3,
                LocalDate.now().minusMonths(1));
        recordPayment(l1, "300000", LocalDate.now().minusDays(10));

        Loan l2 = createLoan(lenderA, c2, "1000000", "12", 6,
                LocalDate.now().minusMonths(4)); // will be overdue
        recordPayment(l2, "200000", LocalDate.now().minusMonths(3));

        Loan l3 = createLoan(lenderA, c3, "250000", "8", 2,
                LocalDate.now().minusMonths(2));
        recordPayment(l3, "270000", LocalDate.now().minusDays(5)); // fully paid

        // ---- Loans for Lender B ----
        Loan l4 = createLoan(lenderB, c4, "750000", "15", 4,
                LocalDate.now().minusMonths(1));
        Loan l5 = createLoan(lenderB, c5, "400000", "10", 3,
                LocalDate.now().minusDays(15));
        recordPayment(l5, "150000", LocalDate.now().minusDays(2));

        refreshAll();
    }

    private Lender createLender(String name, String business, String phone,
                                String email, UserStatus status) {
        User u = User.builder()
                .fullName(name)
                .email(email)
                .phone(phone)
                .password(encoder.encode("lender123"))
                .role(Role.LENDER)
                .status(status)
                .build();
        u = userRepository.save(u);

        Lender lender = Lender.builder()
                .user(u)
                .businessName(business)
                .approvalStatus(status)
                .build();
        return lenderRepository.save(lender);
    }

    private Customer createCustomer(Lender lender, String name, String phone,
                                    String altPhone, String nida) {
        Customer c = Customer.builder()
                .lender(lender)
                .fullName(name)
                .phone(phone)
                .alternativePhone(altPhone)
                .nida(nida)
                .build();
        return customerRepository.save(c);
    }

    private Loan createLoan(Lender lender, Customer customer, String amount,
                            String rate, int months, LocalDate start) {
        BigDecimal amt = new BigDecimal(amount);
        BigDecimal r = new BigDecimal(rate);
        BigDecimal total = LoanCalculator.totalRepayment(amt, r);
        Loan loan = Loan.builder()
                .customer(customer)
                .lender(lender)
                .amount(amt)
                .interestRate(r)
                .duration(months)
                .startDate(start)
                .dueDate(start.plusMonths(months))
                .totalRepayment(total)
                .amountPaid(BigDecimal.ZERO)
                .remainingBalance(total)
                .status(LoanStatus.ACTIVE)
                .build();
        return loanRepository.save(loan);
    }

    private void recordPayment(Loan loan, String amount, LocalDate date) {
        BigDecimal pay = new BigDecimal(amount);
        BigDecimal newPaid = loan.getAmountPaid().add(pay);
        BigDecimal newBalance = loan.getTotalRepayment().subtract(newPaid);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            newBalance = BigDecimal.ZERO;
            newPaid = loan.getTotalRepayment();
        }
        loan.setAmountPaid(newPaid);
        loan.setRemainingBalance(newBalance);
        loan.setStatus(LoanCalculator.deriveStatus(loan));
        loanRepository.save(loan);

        Payment p = Payment.builder()
                .loan(loan)
                .lender(loan.getLender())
                .amountPaid(pay)
                .paymentDate(date)
                .balanceAfter(newBalance)
                .build();
        paymentRepository.save(p);
    }

    private void refreshAll() {
        loanRepository.findAll().forEach(loan -> {
            loan.setStatus(LoanCalculator.deriveStatus(loan));
            loanRepository.save(loan);
        });
    }
}
