package com.loansaas.repository;

import com.loansaas.entity.Customer;
import com.loansaas.entity.Lender;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    List<Customer> findByLenderOrderByCreatedAtDesc(Lender lender);
    long countByLender(Lender lender);
    Optional<Customer> findByIdAndLender(Long id, Lender lender);
    boolean existsByNida(String nida);
    boolean existsByPhone(String phone);
    Optional<Customer> findByNida(String nida);
    Optional<Customer> findByPhone(String phone);

    List<Customer> findByLenderAndFullNameContainingIgnoreCaseOrLenderAndPhoneContainingIgnoreCase(
            Lender lender1, String name, Lender lender2, String phone);
}
