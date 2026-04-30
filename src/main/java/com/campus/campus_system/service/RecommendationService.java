package com.campus.campus_system.service;

import com.campus.campus_system.ai.AiSemanticRecommendationClient;
import com.campus.campus_system.ai.SemanticScoreItem;
import com.campus.campus_system.ai.SemanticScoreRequest;
import com.campus.campus_system.ai.SemanticScoreResult;
import com.campus.campus_system.entity.HelpRequest;
import com.campus.campus_system.entity.JobPosting;
import com.campus.campus_system.entity.SecondHandProduct;
import com.campus.campus_system.entity.UserPreference;
import com.campus.campus_system.recommendation.ScoredRecommendation;
import com.campus.campus_system.repository.HelpRequestRepository;
import com.campus.campus_system.repository.JobPostingRepository;
import com.campus.campus_system.repository.SecondHandProductRepository;
import com.campus.campus_system.repository.UserPreferenceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class RecommendationService {
    private static final Logger log = LoggerFactory.getLogger(RecommendationService.class);
    private static final int RECOMMENDATION_LIMIT = 10;
    private static final int CANDIDATE_LIMIT = 20;

    private final UserPreferenceRepository preferenceRepository;
    private final SecondHandProductRepository productRepository;
    private final JobPostingRepository jobPostingRepository;
    private final HelpRequestRepository helpRequestRepository;
    private final AiSemanticRecommendationClient aiSemanticRecommendationClient;
    private final UserProfileService userProfileService;

    public RecommendationService(UserPreferenceRepository preferenceRepository,
                                 SecondHandProductRepository productRepository,
                                 JobPostingRepository jobPostingRepository,
                                 HelpRequestRepository helpRequestRepository,
                                 AiSemanticRecommendationClient aiSemanticRecommendationClient,
                                 UserProfileService userProfileService) {
        this.preferenceRepository = preferenceRepository;
        this.productRepository = productRepository;
        this.jobPostingRepository = jobPostingRepository;
        this.helpRequestRepository = helpRequestRepository;
        this.aiSemanticRecommendationClient = aiSemanticRecommendationClient;
        this.userProfileService = userProfileService;
    }

    @Transactional
    public void recordPreference(Long userId, String category, String keyword) {
        Optional<UserPreference> existing = preferenceRepository.findByUserId(userId).stream()
                .filter(preference -> category.equals(preference.getCategory()) && keyword.equals(preference.getKeyword()))
                .findFirst();

        if (existing.isPresent()) {
            UserPreference preference = existing.get();
            preference.setClickCount(preference.getClickCount() + 1);
            preference.setLastClickTime(LocalDateTime.now());
            preferenceRepository.save(preference);
            return;
        }

        UserPreference preference = new UserPreference();
        preference.setUserId(userId);
        preference.setCategory(category);
        preference.setKeyword(keyword);
        preference.setClickCount(1);
        preferenceRepository.save(preference);
    }

    public List<ScoredRecommendation<SecondHandProduct>> getRecommendedProducts(Long userId) {
        List<UserPreference> preferences = loadPreferences(userId, "product");
        UserProfileService.ProfileSnapshot profile = userProfileService.buildProfile(userId, "product", preferences);
        List<SecondHandProduct> candidates = buildProductCandidates(profile.recallKeywords());
        return rankCandidates(
                userId,
                "product",
                candidates,
                profile,
                this::buildProductSemanticText,
                this::buildLegacyProductRecommendations,
                Comparator.comparingInt(SecondHandProduct::getViewCount).reversed()
        );
    }

    public List<ScoredRecommendation<JobPosting>> getRecommendedJobs(Long userId) {
        List<UserPreference> preferences = loadPreferences(userId, "job");
        UserProfileService.ProfileSnapshot profile = userProfileService.buildProfile(userId, "job", preferences);
        List<JobPosting> candidates = buildJobCandidates(profile.recallKeywords());
        return rankCandidates(
                userId,
                "job",
                candidates,
                profile,
                this::buildJobSemanticText,
                this::buildLegacyJobRecommendations,
                Comparator.comparingInt(JobPosting::getViewCount).reversed()
        );
    }

    public List<ScoredRecommendation<HelpRequest>> getRecommendedHelps(Long userId) {
        List<UserPreference> preferences = loadPreferences(userId, "help");
        UserProfileService.ProfileSnapshot profile = userProfileService.buildProfile(userId, "help", preferences);
        List<HelpRequest> candidates = buildHelpCandidates(profile.recallKeywords());
        return rankCandidates(
                userId,
                "help",
                candidates,
                profile,
                this::buildHelpSemanticText,
                this::buildLegacyHelpRecommendations,
                Comparator.<HelpRequest>comparingInt(this::helpUrgencyRank).reversed()
                        .thenComparing(HelpRequest::getViewCount, Comparator.reverseOrder())
        );
    }

    private <T> List<ScoredRecommendation<T>> rankCandidates(Long userId,
                                                             String category,
                                                             List<T> candidates,
                                                             UserProfileService.ProfileSnapshot profile,
                                                             Function<T, String> textBuilder,
                                                             Function<List<String>, List<T>> legacyBuilder,
                                                             Comparator<T> fallbackComparator) {
        log.info("AI recommend: userId={}", userId);
        log.info("AI recommend: category={}, behaviorBased={}, signalCount={}", category, profile.behaviorBased(), profile.signalCount());
        log.info("AI recommend: userProfile={}", profile.profileText());
        log.info("AI recommend: candidateCount={}", candidates.size());

        if (candidates.isEmpty()) {
            log.info("AI recommend: candidate list is empty");
            return List.of();
        }

        if (profile.profileText().isBlank() || profile.recallKeywords().isEmpty()) {
            log.info("AI recommend: profile is empty, fallback to rule-based ranking");
            return toFallbackRecommendations(legacyBuilder.apply(profile.recallKeywords()), fallbackComparator);
        }

        try {
            List<SemanticScoreItem> items = candidates.stream()
                    .map(candidate -> new SemanticScoreItem(resolveItemId(candidate), textBuilder.apply(candidate)))
                    .toList();
            List<SemanticScoreResult> semanticScores = aiSemanticRecommendationClient.scoreItems(
                    new SemanticScoreRequest(profile.profileText(), items)
            );
            log.info("AI recommend: aiResultCount={}", semanticScores.size());
            log.info("AI recommend: semantic ranking enabled");

            Map<String, Double> semanticScoreMap = semanticScores.stream()
                    .collect(Collectors.toMap(SemanticScoreResult::itemId, SemanticScoreResult::semanticScore, (left, right) -> left));

            int maxViews = candidates.stream().mapToInt(this::resolveViewCount).max().orElse(0);
            List<ScoredRecommendation<T>> scored = new ArrayList<>();
            for (T candidate : candidates) {
                double semanticScore = clampScore(semanticScoreMap.getOrDefault(resolveItemId(candidate), 0.0));
                double hotScore = normalizeHotScore(resolveViewCount(candidate), maxViews);
                double freshnessScore = calculateFreshnessScore(resolveCreateTime(candidate));
                double finalScore = semanticScore * 0.6 + hotScore * 0.25 + freshnessScore * 0.15;
                scored.add(new ScoredRecommendation<>(candidate, semanticScore, hotScore, freshnessScore, finalScore, false));
            }

            return scored.stream()
                    .sorted(Comparator.comparingDouble(ScoredRecommendation<T>::getFinalScore).reversed())
                    .limit(RECOMMENDATION_LIMIT)
                    .toList();
        } catch (RuntimeException ex) {
            log.warn("AI recommend: AI service unavailable, fallback to rule-based ranking", ex);
            return toFallbackRecommendations(legacyBuilder.apply(profile.recallKeywords()), fallbackComparator);
        }
    }

    private List<UserPreference> loadPreferences(Long userId, String category) {
        return preferenceRepository.findTopPreferencesByUserId(userId).stream()
                .filter(preference -> category.equals(preference.getCategory()))
                .toList();
    }

    private List<SecondHandProduct> buildProductCandidates(List<String> recallKeywords) {
        List<SecondHandProduct> allProducts = productRepository.findByStatus("on_sale");
        return buildCandidates(allProducts, recallKeywords, productRepository::search);
    }

    private List<JobPosting> buildJobCandidates(List<String> recallKeywords) {
        List<JobPosting> allJobs = jobPostingRepository.findByStatus("active");
        return buildCandidates(allJobs, recallKeywords, jobPostingRepository::search);
    }

    private List<HelpRequest> buildHelpCandidates(List<String> recallKeywords) {
        List<HelpRequest> allHelps = helpRequestRepository.findByStatus("pending");
        return buildCandidates(allHelps, recallKeywords, helpRequestRepository::search);
    }

    private <T> List<T> buildCandidates(List<T> allItems,
                                        List<String> recallKeywords,
                                        Function<String, List<T>> recallByKeyword) {
        Map<String, T> candidates = new LinkedHashMap<>();

        for (String keyword : recallKeywords) {
            if (keyword == null || keyword.isBlank()) {
                continue;
            }
            for (T item : recallByKeyword.apply(keyword)) {
                candidates.put(resolveItemId(item), item);
            }
        }

        allItems.stream()
                .sorted(Comparator.comparingInt(this::resolveViewCount).reversed())
                .limit(CANDIDATE_LIMIT)
                .forEach(item -> candidates.putIfAbsent(resolveItemId(item), item));

        allItems.stream()
                .sorted(Comparator.comparing(this::resolveCreateTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(CANDIDATE_LIMIT)
                .forEach(item -> candidates.putIfAbsent(resolveItemId(item), item));

        return candidates.values().stream()
                .limit(CANDIDATE_LIMIT)
                .toList();
    }

    private List<SecondHandProduct> buildLegacyProductRecommendations(List<String> recallKeywords) {
        return buildLegacyRecommendations(
                recallKeywords,
                productRepository.findByStatus("on_sale"),
                productRepository::search,
                Comparator.comparingInt(SecondHandProduct::getViewCount).reversed()
        );
    }

    private List<JobPosting> buildLegacyJobRecommendations(List<String> recallKeywords) {
        return buildLegacyRecommendations(
                recallKeywords,
                jobPostingRepository.findByStatus("active"),
                jobPostingRepository::search,
                Comparator.comparingInt(JobPosting::getViewCount).reversed()
        );
    }

    private List<HelpRequest> buildLegacyHelpRecommendations(List<String> recallKeywords) {
        Comparator<HelpRequest> comparator = Comparator.<HelpRequest>comparingInt(this::helpUrgencyRank).reversed()
                .thenComparing(HelpRequest::getViewCount, Comparator.reverseOrder());
        return buildLegacyRecommendations(
                recallKeywords,
                helpRequestRepository.findByStatus("pending"),
                helpRequestRepository::search,
                comparator
        );
    }

    private <T> List<T> buildLegacyRecommendations(List<String> recallKeywords,
                                                   List<T> defaultItems,
                                                   Function<String, List<T>> recallByKeyword,
                                                   Comparator<T> comparator) {
        if (recallKeywords == null || recallKeywords.isEmpty()) {
            return defaultItems.stream()
                    .sorted(comparator)
                    .limit(RECOMMENDATION_LIMIT)
                    .toList();
        }

        Map<String, T> recommended = new LinkedHashMap<>();
        for (String keyword : recallKeywords) {
            if (keyword == null || keyword.isBlank()) {
                continue;
            }
            for (T item : recallByKeyword.apply(keyword)) {
                recommended.put(resolveItemId(item), item);
            }
        }

        if (recommended.isEmpty()) {
            return defaultItems.stream()
                    .sorted(comparator)
                    .limit(RECOMMENDATION_LIMIT)
                    .toList();
        }

        return recommended.values().stream()
                .sorted(comparator)
                .limit(RECOMMENDATION_LIMIT)
                .toList();
    }

    private String buildProductSemanticText(SecondHandProduct product) {
        return String.join(" | ",
                safe(product.getTitle()),
                safe(product.getCategory()),
                safe(product.getDescription()));
    }

    private String buildJobSemanticText(JobPosting job) {
        return String.join(" | ",
                safe(job.getTitle()),
                safe(job.getCompany()),
                safe(job.getLocation()),
                safe(job.getJobType()),
                safe(job.getDescription()),
                safe(job.getRequirements()));
    }

    private String buildHelpSemanticText(HelpRequest help) {
        return String.join(" | ",
                safe(help.getTitle()),
                safe(help.getCategory()),
                safe(help.getLocation()),
                safe(help.getUrgency()),
                safe(help.getDescription()));
    }

    private <T> List<ScoredRecommendation<T>> toFallbackRecommendations(List<T> items, Comparator<T> comparator) {
        int maxViews = items.stream().mapToInt(this::resolveViewCount).max().orElse(0);
        return items.stream()
                .sorted(comparator)
                .limit(RECOMMENDATION_LIMIT)
                .map(item -> {
                    double hotScore = normalizeHotScore(resolveViewCount(item), maxViews);
                    double freshnessScore = calculateFreshnessScore(resolveCreateTime(item));
                    double finalScore = hotScore * 0.25 + freshnessScore * 0.15;
                    return new ScoredRecommendation<>(item, 0.0, hotScore, freshnessScore, finalScore, true);
                })
                .toList();
    }

    private String resolveItemId(Object item) {
        if (item instanceof SecondHandProduct product) {
            return String.valueOf(product.getId());
        }
        if (item instanceof JobPosting job) {
            return String.valueOf(job.getId());
        }
        if (item instanceof HelpRequest help) {
            return String.valueOf(help.getId());
        }
        throw new IllegalArgumentException("Unsupported recommendation item");
    }

    private int resolveViewCount(Object item) {
        if (item instanceof SecondHandProduct product) {
            return product.getViewCount() == null ? 0 : product.getViewCount();
        }
        if (item instanceof JobPosting job) {
            return job.getViewCount() == null ? 0 : job.getViewCount();
        }
        if (item instanceof HelpRequest help) {
            return help.getViewCount() == null ? 0 : help.getViewCount();
        }
        return 0;
    }

    private LocalDateTime resolveCreateTime(Object item) {
        if (item instanceof SecondHandProduct product) {
            return product.getCreateTime();
        }
        if (item instanceof JobPosting job) {
            return job.getCreateTime();
        }
        if (item instanceof HelpRequest help) {
            return help.getCreateTime();
        }
        return null;
    }

    private int helpUrgencyRank(HelpRequest help) {
        if ("high".equalsIgnoreCase(help.getUrgency())) {
            return 3;
        }
        if ("medium".equalsIgnoreCase(help.getUrgency())) {
            return 2;
        }
        if ("low".equalsIgnoreCase(help.getUrgency())) {
            return 1;
        }
        return 0;
    }

    private double normalizeHotScore(int viewCount, int maxViews) {
        if (maxViews <= 0) {
            return 0.0;
        }
        return clampScore((double) viewCount / (double) maxViews);
    }

    private double calculateFreshnessScore(LocalDateTime createTime) {
        if (createTime == null) {
            return 0.0;
        }
        long ageDays = Math.max(0, Duration.between(createTime, LocalDateTime.now()).toDays());
        return clampScore(1.0 / (1.0 + (ageDays / 7.0)));
    }

    private double clampScore(double score) {
        if (Double.isNaN(score) || Double.isInfinite(score)) {
            return 0.0;
        }
        return Math.max(0.0, Math.min(1.0, score));
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
