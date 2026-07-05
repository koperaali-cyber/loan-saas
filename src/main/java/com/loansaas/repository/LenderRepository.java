package com.loansaas.repository;

import com.loansaas.entity.Lender;
import com.loansaas.entity.User;
import com.loansaas.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LenderRepository extends JpaRepository<Lender, Long> {
    Optional<Lender> findByUser(User user);
    Optional<Lender> findByUserId(Long userId);
    List<Lender> findByApprovalStatus(UserStatus status);
    long countByApprovalStatus(UserStatus status);
}
