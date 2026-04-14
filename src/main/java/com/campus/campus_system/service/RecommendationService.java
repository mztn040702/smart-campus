package com.campus.campus_system.service;

import com.campus.campus_system.entity.*;
import com.campus.campus_system.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {
    @Autowired
    private UserPreferenceRepository preferenceRepository;

    @Autowired
    private SecondHandProductRepository productRepository;

    @Autowired
    private JobPostingRepository jobPostingRepository;

    @Autowired
    private HelpRequestRepository helpRequestRepository;

    // 记录用户偏好（点击商品、职位、求助时调用）
    @Transactional
    public void recordPreference(Long userId, String category, String keyword) {
        Optional<UserPreference> existing = preferenceRepository.findByUserId(userId).stream()
                .filter(p -> category.equals(p.getCategory()) && keyword.equals(p.getKeyword()))
                .findFirst();

        if (existing.isPresent()) {
            UserPreference pref = existing.get();
            pref.setClickCount(pref.getClickCount() + 1);
            pref.setLastClickTime(java.time.LocalDateTime.now());
            preferenceRepository.save(pref);
        } else {
            UserPreference pref = new UserPreference();
            pref.setUserId(userId);
            pref.setCategory(category);
            pref.setKeyword(keyword);
            pref.setClickCount(1);
            preferenceRepository.save(pref);
        }
    }

    // 获取推荐商品
    public List<SecondHandProduct> getRecommendedProducts(Long userId) {
        List<UserPreference> preferences = preferenceRepository.findTopPreferencesByUserId(userId);
        if (preferences.isEmpty()) {
            // 如果没有偏好，返回热门商品（按浏览次数）
            return productRepository.findByStatus("on_sale").stream()
                    .sorted((a, b) -> b.getViewCount().compareTo(a.getViewCount()))
                    .limit(10)
                    .collect(Collectors.toList());
        }

        // 根据用户偏好推荐
        Set<String> keywords = preferences.stream()
                .filter(p -> "product".equals(p.getCategory()))
                .map(UserPreference::getKeyword)
                .collect(Collectors.toSet());

        if (keywords.isEmpty()) {
            return productRepository.findByStatus("on_sale").stream()
                    .sorted((a, b) -> b.getViewCount().compareTo(a.getViewCount()))
                    .limit(10)
                    .collect(Collectors.toList());
        }

        List<SecondHandProduct> recommended = new ArrayList<>();
        for (String keyword : keywords) {
            recommended.addAll(productRepository.search(keyword));
        }
        return recommended.stream()
                .distinct()
                .sorted((a, b) -> b.getViewCount().compareTo(a.getViewCount()))
                .limit(10)
                .collect(Collectors.toList());
    }

    // 获取推荐职位
    public List<JobPosting> getRecommendedJobs(Long userId) {
        List<UserPreference> preferences = preferenceRepository.findTopPreferencesByUserId(userId);
        if (preferences.isEmpty()) {
            return jobPostingRepository.findByStatus("active").stream()
                    .sorted((a, b) -> b.getViewCount().compareTo(a.getViewCount()))
                    .limit(10)
                    .collect(Collectors.toList());
        }

        Set<String> keywords = preferences.stream()
                .filter(p -> "job".equals(p.getCategory()))
                .map(UserPreference::getKeyword)
                .collect(Collectors.toSet());

        if (keywords.isEmpty()) {
            return jobPostingRepository.findByStatus("active").stream()
                    .sorted((a, b) -> b.getViewCount().compareTo(a.getViewCount()))
                    .limit(10)
                    .collect(Collectors.toList());
        }

        List<JobPosting> recommended = new ArrayList<>();
        for (String keyword : keywords) {
            recommended.addAll(jobPostingRepository.search(keyword));
        }
        return recommended.stream()
                .distinct()
                .sorted((a, b) -> b.getViewCount().compareTo(a.getViewCount()))
                .limit(10)
                .collect(Collectors.toList());
    }

    // 获取推荐求助
    public List<HelpRequest> getRecommendedHelps(Long userId) {
        List<UserPreference> preferences = preferenceRepository.findTopPreferencesByUserId(userId);
        if (preferences.isEmpty()) {
            return helpRequestRepository.findByStatus("pending").stream()
                    .sorted((a, b) -> {
                        // 按紧急程度和浏览次数排序
                        int urgencyOrder = Map.of("high", 3, "medium", 2, "low", 1).getOrDefault(a.getUrgency(), 0)
                                .compareTo(Map.of("high", 3, "medium", 2, "low", 1).getOrDefault(b.getUrgency(), 0));
                        if (urgencyOrder != 0) return -urgencyOrder;
                        return b.getViewCount().compareTo(a.getViewCount());
                    })
                    .limit(10)
                    .collect(Collectors.toList());
        }

        Set<String> keywords = preferences.stream()
                .filter(p -> "help".equals(p.getCategory()))
                .map(UserPreference::getKeyword)
                .collect(Collectors.toSet());

        if (keywords.isEmpty()) {
            return helpRequestRepository.findByStatus("pending").stream()
                    .sorted((a, b) -> {
                        int urgencyOrder = Map.of("high", 3, "medium", 2, "low", 1).getOrDefault(a.getUrgency(), 0)
                                .compareTo(Map.of("high", 3, "medium", 2, "low", 1).getOrDefault(b.getUrgency(), 0));
                        if (urgencyOrder != 0) return -urgencyOrder;
                        return b.getViewCount().compareTo(a.getViewCount());
                    })
                    .limit(10)
                    .collect(Collectors.toList());
        }

        List<HelpRequest> recommended = new ArrayList<>();
        for (String keyword : keywords) {
            recommended.addAll(helpRequestRepository.search(keyword));
        }
        return recommended.stream()
                .distinct()
                .sorted((a, b) -> {
                    int urgencyOrder = Map.of("high", 3, "medium", 2, "low", 1).getOrDefault(a.getUrgency(), 0)
                            .compareTo(Map.of("high", 3, "medium", 2, "low", 1).getOrDefault(b.getUrgency(), 0));
                    if (urgencyOrder != 0) return -urgencyOrder;
                    return b.getViewCount().compareTo(a.getViewCount());
                })
                .limit(10)
                .collect(Collectors.toList());
    }
}

