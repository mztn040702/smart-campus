package com.campus.campus_system.service;

import com.campus.campus_system.entity.UserBehavior;
import com.campus.campus_system.repository.UserBehaviorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UserBehaviorService {
    private final UserBehaviorRepository userBehaviorRepository;

    public UserBehaviorService(UserBehaviorRepository userBehaviorRepository) {
        this.userBehaviorRepository = userBehaviorRepository;
    }

    @Transactional
    public void recordSearch(Long userId, String category, String targetType, String keyword) {
        if (userId == null || keyword == null || keyword.isBlank()) {
            return;
        }
        saveBehavior(userId, category, "SEARCH", targetType, null, keyword.trim(), null);
    }

    @Transactional
    public void recordView(Long userId, String category, String targetType, Long targetId, String contentTitle) {
        if (userId == null || targetId == null) {
            return;
        }
        saveBehavior(userId, category, "VIEW", targetType, String.valueOf(targetId), null, contentTitle);
    }

    private void saveBehavior(Long userId, String category, String behaviorType, String targetType,
                              String targetId, String keyword, String contentTitle) {
        UserBehavior behavior = new UserBehavior();
        behavior.setUserId(userId);
        behavior.setCategory(category);
        behavior.setBehaviorType(behaviorType);
        behavior.setTargetType(targetType);
        behavior.setTargetId(targetId);
        behavior.setKeyword(keyword);
        behavior.setContentTitle(contentTitle);
        behavior.setBehaviorTime(LocalDateTime.now());
        userBehaviorRepository.save(behavior);
    }
}
