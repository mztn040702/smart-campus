package com.campus.campus_system.service;

import com.campus.campus_system.entity.UserBehavior;
import com.campus.campus_system.entity.UserPreference;
import com.campus.campus_system.repository.UserBehaviorRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserProfileService {
    private static final int MAX_BEHAVIORS = 15;

    private final UserBehaviorRepository userBehaviorRepository;

    public UserProfileService(UserBehaviorRepository userBehaviorRepository) {
        this.userBehaviorRepository = userBehaviorRepository;
    }

    public ProfileSnapshot buildProfile(Long userId, String category, List<UserPreference> preferences) {
        List<UserBehavior> recentBehaviors = userBehaviorRepository.findRecentByUserIdAndCategory(userId, category).stream()
                .limit(MAX_BEHAVIORS)
                .toList();

        if (!recentBehaviors.isEmpty()) {
            return fromBehaviors(category, recentBehaviors);
        }
        return fromPreferences(category, preferences);
    }

    private ProfileSnapshot fromBehaviors(String category, List<UserBehavior> behaviors) {
        StringBuilder profileBuilder = new StringBuilder();
        profileBuilder.append("user behavior in ").append(category).append(": ");
        Set<String> recallKeywords = new LinkedHashSet<>();

        for (UserBehavior behavior : behaviors) {
            if (behavior.getKeyword() != null && !behavior.getKeyword().isBlank()) {
                recallKeywords.add(behavior.getKeyword().trim());
                appendWeighted(profileBuilder, behavior.getKeyword().trim(), behaviorWeight(behavior.getBehaviorType()));
            }
            if (behavior.getContentTitle() != null && !behavior.getContentTitle().isBlank()) {
                recallKeywords.add(behavior.getContentTitle().trim());
                appendWeighted(profileBuilder, behavior.getContentTitle().trim(), behaviorWeight(behavior.getBehaviorType()) + 1);
            }
        }

        return new ProfileSnapshot(profileBuilder.toString().trim(), new ArrayList<>(recallKeywords), true, behaviors.size());
    }

    private ProfileSnapshot fromPreferences(String category, List<UserPreference> preferences) {
        StringBuilder profileBuilder = new StringBuilder();
        profileBuilder.append("user preference in ").append(category).append(": ");
        Set<String> recallKeywords = new LinkedHashSet<>();

        for (UserPreference preference : preferences) {
            if (preference.getKeyword() == null || preference.getKeyword().isBlank()) {
                continue;
            }
            String keyword = preference.getKeyword().trim();
            recallKeywords.add(keyword);
            appendWeighted(profileBuilder, keyword, Math.max(1, Math.min(preference.getClickCount(), 3)));
        }

        return new ProfileSnapshot(profileBuilder.toString().trim(), new ArrayList<>(recallKeywords), false, preferences.size());
    }

    private void appendWeighted(StringBuilder builder, String value, int repeat) {
        for (int i = 0; i < repeat; i++) {
            builder.append(value).append(". ");
        }
    }

    private int behaviorWeight(String behaviorType) {
        if ("VIEW".equalsIgnoreCase(behaviorType)) {
            return 3;
        }
        if ("SEARCH".equalsIgnoreCase(behaviorType)) {
            return 2;
        }
        if ("PUBLISH".equalsIgnoreCase(behaviorType)) {
            return 2;
        }
        return 1;
    }

    public record ProfileSnapshot(String profileText, List<String> recallKeywords, boolean behaviorBased, int signalCount) {
    }
}
