package com.campus.campus_system;

import com.campus.campus_system.entity.HelpRequest;
import com.campus.campus_system.entity.JobPosting;
import com.campus.campus_system.entity.SecondHandProduct;
import com.campus.campus_system.entity.User;
import com.campus.campus_system.repository.HelpRequestRepository;
import com.campus.campus_system.repository.JobPostingRepository;
import com.campus.campus_system.repository.SecondHandProductRepository;
import com.campus.campus_system.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminFeatureTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SecondHandProductRepository productRepository;

    @Autowired
    private JobPostingRepository jobPostingRepository;

    @Autowired
    private HelpRequestRepository helpRequestRepository;

    @Test
    void bootstrapsDefaultAdminAccount() {
        User admin = userRepository.findByUsername("admin").orElseThrow();

        assertThat(admin.getRole()).isEqualTo("ADMIN");
        assertThat(admin.getStatus()).isEqualTo("ACTIVE");
        assertThat(admin.getPassword()).startsWith("$2");
    }

    @Test
    void normalUserCannotAccessAdminApis() throws Exception {
        String username = "user_" + UUID.randomUUID().toString().replace("-", "");
        String password = "Secret123!";

        registerUser(username, password);
        String token = loginAndExtractToken(username, password);

        mockMvc.perform(get("/api/admin/stats")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void adminCanAccessStatsUsersAndModerationApis() throws Exception {
        String adminToken = loginAndExtractToken("admin", "Admin123!");
        User seller = createUserEntity("seller_" + UUID.randomUUID().toString().replace("-", ""));

        SecondHandProduct product = new SecondHandProduct();
        product.setSellerId(seller.getId());
        product.setTitle("blocked-product-" + UUID.randomUUID());
        product.setDescription("test product");
        product.setPrice(new BigDecimal("12.50"));
        product.setCategory("books");
        product = productRepository.save(product);

        JobPosting job = new JobPosting();
        job.setPublisherId(seller.getId());
        job.setTitle("blocked-job-" + UUID.randomUUID());
        job.setDescription("test job");
        job.setCompany("Campus");
        job.setLocation("Dorm");
        job.setJobType("Part-time");
        job = jobPostingRepository.save(job);

        HelpRequest help = new HelpRequest();
        help.setRequesterId(seller.getId());
        help.setTitle("blocked-help-" + UUID.randomUUID());
        help.setDescription("test help");
        help.setCategory("delivery");
        help = helpRequestRepository.save(help);

        mockMvc.perform(get("/api/admin/stats")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.userCount").isNumber())
                .andExpect(jsonPath("$.data.productCount").isNumber())
                .andExpect(jsonPath("$.data.jobCount").isNumber())
                .andExpect(jsonPath("$.data.helpCount").isNumber())
                .andExpect(jsonPath("$.data.messageCount").isNumber());

        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").isArray());

        mockMvc.perform(get("/api/admin/products")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/api/admin/jobs")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/api/admin/helps")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(delete("/api/admin/products/{id}", product.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(delete("/api/admin/jobs/{id}", job.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(delete("/api/admin/helps/{id}", help.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        assertThat(productRepository.findById(product.getId())).isEmpty();
        assertThat(jobPostingRepository.findById(job.getId())).isEmpty();
        assertThat(helpRequestRepository.findById(help.getId())).isEmpty();
    }

    @Test
    void adminCanDisableAndEnableUserAndDisabledUserCannotLogin() throws Exception {
        String adminToken = loginAndExtractToken("admin", "Admin123!");
        String username = "toggle_" + UUID.randomUUID().toString().replace("-", "");
        String password = "Secret123!";

        registerUser(username, password);
        User user = userRepository.findByUsername(username).orElseThrow();

        mockMvc.perform(post("/api/admin/users/{id}/disable", user.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("DISABLED"));

        User disabledUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(disabledUser.getStatus()).isEqualTo("DISABLED");

        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "password": "%s"
                                }
                                """.formatted(username, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.msg").value("Account disabled"));

        mockMvc.perform(post("/api/admin/users/{id}/enable", user.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "password": "%s"
                                }
                                """.formatted(username, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.token").isString());
    }

    private void registerUser(String username, String password) throws Exception {
        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "password": "%s",
                                  "realName": "Test User",
                                  "college": "Computer Science"
                                }
                                """.formatted(username, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    private User createUserEntity(String username) {
        User user = new User();
        user.setUsername(username);
        user.setPassword("$2a$10$abcdefghijklmnopqrstuv");
        user.setRealName("Seller");
        user.setCollege("Computer Science");
        user.setRole("USER");
        user.setStatus("ACTIVE");
        user.setCreateTime(LocalDateTime.now());
        return userRepository.save(user);
    }

    private String loginAndExtractToken(String username, String password) throws Exception {
        String response = mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "password": "%s"
                                }
                                """.formatted(username, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.token").isString())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return response.replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");
    }
}
