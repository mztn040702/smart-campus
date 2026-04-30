package com.campus.campus_system.controller;

import com.campus.campus_system.entity.HelpRequest;
import com.campus.campus_system.entity.JobPosting;
import com.campus.campus_system.entity.SecondHandProduct;
import com.campus.campus_system.recommendation.ScoredRecommendation;
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
            List<Map<String, Object>> products = recommendationService.getRecommendedProducts(userId).stream()
                    .map(this::toProductResponse)
                    .toList();
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
            List<Map<String, Object>> jobs = recommendationService.getRecommendedJobs(userId).stream()
                    .map(this::toJobResponse)
                    .toList();
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
            List<Map<String, Object>> helps = recommendationService.getRecommendedHelps(userId).stream()
                    .map(this::toHelpResponse)
                    .toList();
            result.put("code", 0);
            result.put("data", helps);
        } catch (Exception e) {
            result.put("code", 1);
            result.put("msg", e.getMessage());
        }
        return result;
    }

    private Map<String, Object> toProductResponse(ScoredRecommendation<SecondHandProduct> recommendation) {
        SecondHandProduct product = recommendation.getItem();
        Map<String, Object> response = new HashMap<>();
        response.put("id", product.getId());
        response.put("sellerId", product.getSellerId());
        response.put("title", product.getTitle());
        response.put("description", product.getDescription());
        response.put("price", product.getPrice());
        response.put("images", product.getImages());
        response.put("category", product.getCategory());
        response.put("status", product.getStatus());
        response.put("viewCount", product.getViewCount());
        response.put("createTime", product.getCreateTime());
        response.put("updateTime", product.getUpdateTime());
        appendScores(response, recommendation);
        return response;
    }

    private Map<String, Object> toJobResponse(ScoredRecommendation<JobPosting> recommendation) {
        JobPosting job = recommendation.getItem();
        Map<String, Object> response = new HashMap<>();
        response.put("id", job.getId());
        response.put("publisherId", job.getPublisherId());
        response.put("title", job.getTitle());
        response.put("description", job.getDescription());
        response.put("company", job.getCompany());
        response.put("location", job.getLocation());
        response.put("jobType", job.getJobType());
        response.put("salary", job.getSalary());
        response.put("requirements", job.getRequirements());
        response.put("contact", job.getContact());
        response.put("status", job.getStatus());
        response.put("viewCount", job.getViewCount());
        response.put("createTime", job.getCreateTime());
        response.put("updateTime", job.getUpdateTime());
        appendScores(response, recommendation);
        return response;
    }

    private Map<String, Object> toHelpResponse(ScoredRecommendation<HelpRequest> recommendation) {
        HelpRequest help = recommendation.getItem();
        Map<String, Object> response = new HashMap<>();
        response.put("id", help.getId());
        response.put("requesterId", help.getRequesterId());
        response.put("title", help.getTitle());
        response.put("description", help.getDescription());
        response.put("category", help.getCategory());
        response.put("location", help.getLocation());
        response.put("urgency", help.getUrgency());
        response.put("status", help.getStatus());
        response.put("helperId", help.getHelperId());
        response.put("viewCount", help.getViewCount());
        response.put("createTime", help.getCreateTime());
        response.put("updateTime", help.getUpdateTime());
        appendScores(response, recommendation);
        return response;
    }

    private void appendScores(Map<String, Object> response, ScoredRecommendation<?> recommendation) {
        response.put("semanticScore", recommendation.getSemanticScore());
        response.put("hotScore", recommendation.getHotScore());
        response.put("freshnessScore", recommendation.getFreshnessScore());
        response.put("finalScore", recommendation.getFinalScore());
        response.put("fallbackUsed", recommendation.isFallbackUsed());
    }
}
