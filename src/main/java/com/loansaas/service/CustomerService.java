package com.loansaas.service;

import com.loansaas.dto.CustomerDto;
import com.loansaas.entity.*;
import com.loansaas.repository.CustomerRepository;
import com.loansaas.repository.LoanRepository;
import com.loansaas.util.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final LoanRepository loanRepository;
    private final FileStorageService fileStorageService;
    private final ActivityService activityService;

    public List<Customer> listForLender(Lender lender) {
        return customerRepository.findByLenderOrderByCreatedAtDesc(lender);
    }

    public List<Customer> search(Lender lender, String query) {
        if (query == null || query.isBlank()) {
            return listForLender(lender);
        }
        return customerRepository
                .findByLenderAndFullNameContainingIgnoreCaseOrLenderAndPhoneContainingIgnoreCase(
                        lender, query, lender, query);
    }

    public Customer getForLender(Long id, Lender lender) {
        return customerRepository.findByIdAndLender(id, lender)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found or access denied"));
    }

    @Transactional
    public Customer create(CustomerDto dto, Lender lender, User actor) {
        // Fraud prevention: NIDA + phone must be globally unique
        if (dto.getNida() != null && !dto.getNida().isBlank()
                && customerRepository.existsByNida(dto.getNida())) {
            throw new IllegalArgumentException("A customer with this NIDA number already exists.");
        }
        if (customerRepository.existsByPhone(dto.getPhone())) {
            throw new IllegalArgumentException("A customer with this phone number already exists.");
        }

        String photoPath = fileStorageService.store(dto.getPhotoFile(), "customers");

        Customer customer = Customer.builder()
                .lender(lender)
                .fullName(dto.getFullName())
                .phone(dto.getPhone())
                .alternativePhone(emptyToNull(dto.getAlternativePhone()))
                .nida(emptyToNull(dto.getNida()))
                .photo(photoPath)
                .build();
        customer = customerRepository.save(customer);

        activityService.log(actor.getId(), actor.getFullName(),
                "Registered new customer: " + customer.getFullName());
        return customer;
    }

    @Transactional
    public Customer update(Long id, CustomerDto dto, Lender lender, User actor) {
        Customer customer = getForLender(id, lender);

        // Uniqueness checks that ignore this same record
        if (dto.getNida() != null && !dto.getNida().isBlank()) {
            customerRepository.findByNida(dto.getNida()).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new IllegalArgumentException("Another customer already uses this NIDA number.");
                }
            });
        }
        customerRepository.findByPhone(dto.getPhone()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new IllegalArgumentException("Another customer already uses this phone number.");
            }
        });

        customer.setFullName(dto.getFullName());
        customer.setPhone(dto.getPhone());
        customer.setAlternativePhone(emptyToNull(dto.getAlternativePhone()));
        customer.setNida(emptyToNull(dto.getNida()));

        String photoPath = fileStorageService.store(dto.getPhotoFile(), "customers");
        if (photoPath != null) {
            customer.setPhoto(photoPath);
        }
        customer = customerRepository.save(customer);
        activityService.log(actor.getId(), actor.getFullName(),
                "Updated customer: " + customer.getFullName());
        return customer;
    }

    @Transactional
    public void delete(Long id, Lender lender, User actor) {
        Customer customer = getForLender(id, lender);
        if (loanRepository.countByCustomer(customer) > 0) {
            throw new IllegalArgumentException(
                    "Cannot delete a customer who has loan records. Consider keeping the history.");
        }
        String name = customer.getFullName();
        customerRepository.delete(customer);
        activityService.log(actor.getId(), actor.getFullName(), "Deleted customer: " + name);
    }

    // ---- Risk scoring for anti-fraud ----
    public RiskLevel riskLevel(Customer customer) {
        long total = loanRepository.countByCustomer(customer);
        if (total == 0) {
            return RiskLevel.LOW;
        }
        long overdue = loanRepository.countByCustomerAndStatus(customer, LoanStatus.OVERDUE);
        long defaulted = loanRepository.countByCustomerAndStatus(customer, LoanStatus.DEFAULTED);
        long bad = overdue + defaulted;
        double ratio = (double) bad / total;

        if (defaulted > 0 || ratio >= 0.5) {
            return RiskLevel.HIGH;
        }
        if (bad > 0 || ratio >= 0.25) {
            return RiskLevel.MEDIUM;
        }
        return RiskLevel.LOW;
    }

    public long loanCount(Customer customer) {
        return loanRepository.countByCustomer(customer);
    }

    public long paidCount(Customer customer) {
        return loanRepository.countByCustomerAndStatus(customer, LoanStatus.PAID);
    }

    public long lateCount(Customer customer) {
        return loanRepository.countByCustomerAndStatus(customer, LoanStatus.OVERDUE)
                + loanRepository.countByCustomerAndStatus(customer, LoanStatus.DEFAULTED);
    }

    private String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}
