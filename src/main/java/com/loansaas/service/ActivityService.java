package com.loansaas.service;

import com.loansaas.entity.Activity;
import com.loansaas.repository.ActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityRepository activityRepository;

    public void log(Long userId, String userName, String action) {
        Activity activity = Activity.builder()
                .userId(userId)
                .userName(userName)
                .action(action)
                .build();
        activityRepository.save(activity);
    }

    public List<Activity> recentForUser(Long userId, int limit) {
        return activityRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, limit));
    }

    public List<Activity> recentAll(int limit) {
        return activityRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, limit));
    }
}
