package com.campus.campus_system;

import com.campus.campus_system.ai.AiSemanticRecommendationClient;
import com.campus.campus_system.ai.SemanticScoreRequest;
import com.campus.campus_system.ai.SemanticScoreResult;
import com.campus.campus_system.entity.JobPosting;
import com.campus.campus_system.entity.SecondHandProduct;
import com.campus.campus_system.entity.User;
import com.campus.campus_system.entity.UserPreference;
import com.campus.campus_system.repository.JobPostingRepository;
import com.campus.campus_system.repository.SecondHandProductRepository;
import com.campus.campus_system.repository.UserPreferenceRepository;
import com.campus.campus_system.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RecommendationFeatureTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserPreferenceRepository preferenceRepository;

    @Autowired
    private SecondHandProductRepository productRepository;

    @Autowired
    private JobPostingRepository jobPostingRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockBean
    private AiSemanticRecommendationClient aiSemanticRecommendationClient;

    private User currentUser;
    private String authToken;

    @BeforeEach
    void setUp() throws Exception {
        deleteBehaviorRows();
        preferenceRepository.deleteAll();
        jobPostingRepository.deleteAll();
        productRepository.deleteAll();

        String username = "recommend_user_" + UUID.randomUUID().toString().replace("-", "");
        String password = "Secret123!";

        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "password": "%s",
                                  "realName": "Recommend Test",
                                  "college": "Computer Science"
                                }
                                """.formatted(username, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        currentUser = userRepository.findByUsername(username).orElseThrow();

        String loginResponse = mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "password": "%s"
                                }
                                """.formatted(username, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn()
                .getResponse()
                .getContentAsString();

        authToken = "Bearer " + loginResponse.replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");
    }

    @Test
    void productRecommendationsUseHybridScoreWhenAiServiceIsAvailable() throws Exception {
        savePreference(currentUser.getId(), "product", "java books", 6, LocalDateTime.now().minusHours(2));

        SecondHandProduct hotButWeak = saveProduct(
                currentUser.getId(), "Legacy Java Book", "popular but weak semantic match", "books",
                "30.00", 90, LocalDateTime.now().minusDays(12)
        );
        SecondHandProduct semanticWinner = saveProduct(
                currentUser.getId(), "Advanced Java Patterns", "matches the user profile strongly", "books",
                "45.00", 20, LocalDateTime.now().minusDays(1)
        );

        when(aiSemanticRecommendationClient.scoreItems(any()))
                .thenReturn(List.of(
                        new SemanticScoreResult(hotButWeak.getId().toString(), 0.15),
                        new SemanticScoreResult(semanticWinner.getId().toString(), 0.96)
                ));

        mockMvc.perform(get("/api/recommend/products/{userId}", currentUser.getId())
                        .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].id").value(semanticWinner.getId()))
                .andExpect(jsonPath("$.data[0].semanticScore").isNumber())
                .andExpect(jsonPath("$.data[0].hotScore").isNumber())
                .andExpect(jsonPath("$.data[0].freshnessScore").isNumber())
                .andExpect(jsonPath("$.data[0].finalScore").isNumber())
                .andExpect(jsonPath("$.data[0].fallbackUsed").value(false))
                .andExpect(jsonPath("$.data[1].id").value(hotButWeak.getId()));
    }

    @Test
    void jobRecommendationsFallbackWhenAiServiceFails() throws Exception {
        savePreference(currentUser.getId(), "job", "java internship", 5, LocalDateTime.now().minusHours(1));

        JobPosting highViews = saveJob(
                currentUser.getId(), "Java Internship", "classic fallback match", "Campus Lab",
                "Library", "internship", "120", 66, LocalDateTime.now().minusDays(6)
        );
        JobPosting lowViews = saveJob(
                currentUser.getId(), "Java Semantic Favorite", "java internship role that would need ai to win", "Campus Lab",
                "Library", "internship", "110", 12, LocalDateTime.now().minusHours(8)
        );

        when(aiSemanticRecommendationClient.scoreItems(any()))
                .thenThrow(new RuntimeException("AI service offline"));

        mockMvc.perform(get("/api/recommend/jobs/{userId}", currentUser.getId())
                        .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].id").value(highViews.getId()))
                .andExpect(jsonPath("$.data[0].fallbackUsed").value(true))
                .andExpect(jsonPath("$.data[0].semanticScore").value(0.0))
                .andExpect(jsonPath("$.data[0].finalScore").isNumber())
                .andExpect(jsonPath("$.data[1].id").value(lowViews.getId()));
    }

    @Test
    void recommendationProfilePrefersUserBehaviorBeforePreferenceFallback() throws Exception {
        savePreference(currentUser.getId(), "product", "politics notes", 8, LocalDateTime.now().minusHours(5));
        saveBehavior(currentUser.getId(), "product", "SEARCH", "product", null, "Spring Boot project", null, LocalDateTime.now().minusMinutes(20));
        saveBehavior(currentUser.getId(), "product", "VIEW", "product", "101", null, "Java Core Guide", LocalDateTime.now().minusMinutes(10));
        saveBehavior(currentUser.getId(), "product", "VIEW", "product", "102", null, "MySQL Study Notes", LocalDateTime.now().minusMinutes(5));

        SecondHandProduct product = saveProduct(
                currentUser.getId(), "Spring Boot in Action", "Java backend and MySQL training", "books",
                "39.00", 25, LocalDateTime.now().minusHours(12)
        );

        when(aiSemanticRecommendationClient.scoreItems(any()))
                .thenReturn(List.of(new SemanticScoreResult(product.getId().toString(), 0.91)));

        mockMvc.perform(get("/api/recommend/products/{userId}", currentUser.getId())
                        .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].id").value(product.getId()));

        org.mockito.ArgumentCaptor<SemanticScoreRequest> requestCaptor = forClass(SemanticScoreRequest.class);
        org.mockito.Mockito.verify(aiSemanticRecommendationClient).scoreItems(requestCaptor.capture());
        SemanticScoreRequest request = requestCaptor.getValue();
        assertThat(request.userProfileText()).contains("Spring Boot project");
        assertThat(request.userProfileText()).contains("Java Core Guide");
        assertThat(request.userProfileText()).contains("MySQL Study Notes");
        assertThat(request.userProfileText()).doesNotContain("politics notes");
    }

    @Test
    void searchAndViewEndpointsPersistBehaviorRecords() throws Exception {
        SecondHandProduct product = saveProduct(
                currentUser.getId(), "Java Core Guide", "backend fundamentals", "books",
                "28.00", 13, LocalDateTime.now().minusDays(2)
        );

        mockMvc.perform(get("/api/product/list")
                        .header("Authorization", authToken)
                        .param("userId", currentUser.getId().toString())
                        .param("keyword", "Java backend"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/api/product/{id}", product.getId())
                        .header("Authorization", authToken)
                        .param("userId", currentUser.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(product.getId()));

        Integer behaviorCount = jdbcTemplate.queryForObject(
                "select count(*) from user_behavior where user_id = ? and category = 'product'",
                Integer.class,
                currentUser.getId()
        );

        assertThat(behaviorCount).isEqualTo(2);
    }

    private void deleteBehaviorRows() {
        try {
            jdbcTemplate.update("delete from user_behavior");
        } catch (Exception ignored) {
        }
    }

    private void savePreference(Long userId, String category, String keyword, int clickCount, LocalDateTime lastClickTime) {
        UserPreference preference = new UserPreference();
        preference.setUserId(userId);
        preference.setCategory(category);
        preference.setKeyword(keyword);
        preference.setClickCount(clickCount);
        preference.setLastClickTime(lastClickTime);
        preferenceRepository.save(preference);
    }

    private SecondHandProduct saveProduct(Long sellerId, String title, String description, String category,
                                          String price, int viewCount, LocalDateTime createTime) {
        SecondHandProduct product = new SecondHandProduct();
        product.setSellerId(sellerId);
        product.setTitle(title);
        product.setDescription(description);
        product.setCategory(category);
        product.setPrice(new BigDecimal(price));
        product.setViewCount(viewCount);
        product.setStatus("on_sale");
        product.setCreateTime(createTime);
        product.setUpdateTime(createTime);
        return productRepository.save(product);
    }

    private JobPosting saveJob(Long publisherId, String title, String description, String company,
                               String location, String jobType, String salary, int viewCount,
                               LocalDateTime createTime) {
        JobPosting job = new JobPosting();
        job.setPublisherId(publisherId);
        job.setTitle(title);
        job.setDescription(description);
        job.setCompany(company);
        job.setLocation(location);
        job.setJobType(jobType);
        job.setSalary(new BigDecimal(salary));
        job.setViewCount(viewCount);
        job.setStatus("active");
        job.setCreateTime(createTime);
        job.setUpdateTime(createTime);
        return jobPostingRepository.save(job);
    }

    private void saveBehavior(Long userId, String category, String behaviorType, String targetType, String targetId,
                              String keyword, String contentTitle, LocalDateTime behaviorTime) {
        jdbcTemplate.update(
                """
                insert into user_behavior
                (user_id, category, behavior_type, target_type, target_id, keyword, content_title, behavior_time, create_time)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                userId,
                category,
                behaviorType,
                targetType,
                targetId,
                keyword,
                contentTitle,
                behaviorTime,
                behaviorTime
        );
    }
}
