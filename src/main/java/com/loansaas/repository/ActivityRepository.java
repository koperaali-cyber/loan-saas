package com.loansaas.repository;

import com.loansaas.entity.Activity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
    List<Activity> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    List<Activity> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
