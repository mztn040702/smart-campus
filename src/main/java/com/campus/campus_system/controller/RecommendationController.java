package com.campus.campus_system.controller;

import com.campus.campus_system.entity.HelpRequest;
import com.campus.campus_system.entity.JobPosting;
import com.campus.campus_system.entity.SecondHandProduct;
import com.campus.campus_system.service.RecommendationService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recommend")
@CrossOrigin(origins = "*")
public class RecommendationController {
    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @PostMapping("/preference")
    public Map<String, Object> recordPreference(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long userId = Long.valueOf(params.get("userId").toString());
            String category = params.get("category").toString();
            String keyword = params.get("keyword").toString();
            recommendationService.recordPreference(userId, category, keyword);
            result.put("code", 0);
            result.put("msg", "Preference recorded");
        } catch (Exception e) {
            result.put("code", 1);
            result.put("msg", e.getMessage());
        }
        return result;
    }

    @GetMapping("/products/{userId}")
    public Map<String, Object> getRecommendedProducts(@PathVariable Long userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<SecondHandProduct> products = recommendationService.getRecommendedProducts(userId);
            result.put("code", 0);
            result.put("data", products);
        } catch (Exception e) {
            result.put("code", 1);
            result.put("msg", e.getMessage());
        }
        return result;
    }

    @GetMapping("/jobs/{userId}")
    public Map<String, Object> getRecommendedJobs(@PathVariable Long userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<JobPosting> jobs = recommendationService.getRecommendedJobs(userId);
            result.put("code", 0);
            result.put("data", jobs);
        } catch (Exception e) {
            result.put("code", 1);
            result.put("msg", e.getMessage());
        }
        return result;
    }

    @GetMapping("/helps/{userId}")
    public Map<String, Object> getRecommendedHelps(@PathVariable Long userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<HelpRequest> helps = recommendationService.getRecommendedHelps(userId);
            result.put("code", 0);
            result.put("data", helps);
        } catch (Exception e) {
            result.put("code", 1);
            result.put("msg", e.getMessage());
        }
        return result;
    }
}
