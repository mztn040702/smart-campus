package com.campus.campus_system;

import com.campus.campus_system.entity.HelpRequest;
import com.campus.campus_system.entity.JobPosting;
import com.campus.campus_system.entity.SecondHandProduct;
import com.campus.campus_system.entity.User;
import com.campus.campus_system.repository.HelpRequestRepository;
import com.campus.campus_system.repository.JobPostingRepository;
import com.campus.campus_system.repository.SecondHandProductRepository;
import com.campus.campus_system.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SearchFilterFeatureTests {

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

    private String authToken;

    @BeforeEach
    void setUp() throws Exception {
        helpRequestRepository.deleteAll();
        jobPostingRepository.deleteAll();
        productRepository.deleteAll();

        String username = "filter_user_" + UUID.randomUUID().toString().replace("-", "");
        String password = "Secret123!";

        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "password": "%s",
                                  "realName": "Filter Test",
                                  "college": "Computer Science"
                                }
                                """.formatted(username, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

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
    void productListSupportsCombinedFiltersAndSorting() throws Exception {
        User seller = userRepository.findAll().get(userRepository.findAll().size() - 1);

        saveProduct(seller.getId(), "Java Algorithms", "Interview prep", "books", "40.00", 8, "on_sale");
        saveProduct(seller.getId(), "Java Comics", "Entry level", "books", "20.00", 18, "on_sale");
        saveProduct(seller.getId(), "Python Book", "Non target", "books", "25.00", 50, "on_sale");
        saveProduct(seller.getId(), "Java Headset", "Different category", "electronics", "99.00", 5, "on_sale");
        saveProduct(seller.getId(), "Java Old Book", "Off shelf", "books", "10.00", 30, "sold");

        mockMvc.perform(get("/api/product/list")
                        .header("Authorization", authToken)
                        .param("keyword", "Java")
                        .param("category", "books")
                        .param("minPrice", "15")
                        .param("maxPrice", "50")
                        .param("sort", "priceAsc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].title").value("Java Comics"))
                .andExpect(jsonPath("$.data[1].title").value("Java Algorithms"));

        mockMvc.perform(get("/api/product/list")
                        .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.length()").value(4));

        mockMvc.perform(get("/api/product/search")
                        .header("Authorization", authToken)
                        .param("keyword", "Java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.length()").value(3));
    }

    @Test
    void productListAcceptsCanonicalAndLegacyCategoryValues() throws Exception {
        User seller = userRepository.findAll().get(userRepository.findAll().size() - 1);

        saveProduct(seller.getId(), "Legacy Chinese Book", "Legacy seed data", "书籍", "15.00", 3, "on_sale");
        saveProduct(seller.getId(), "Canonical English Book", "Current data", "books", "18.00", 4, "on_sale");
        saveProduct(seller.getId(), "Electronic Device", "Should not match", "electronics", "99.00", 6, "on_sale");

        mockMvc.perform(get("/api/product/list")
                        .header("Authorization", authToken)
                        .param("category", "books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void jobListSupportsKeywordLocationTypeSalaryAndSorting() throws Exception {
        User publisher = userRepository.findAll().get(userRepository.findAll().size() - 1);

        saveJob(publisher.getId(), "Java Tutor", "Java Q and A", "Smart Campus", "Library", "part-time", "120", 12, "active");
        saveJob(publisher.getId(), "Java Lab Assistant", "Java device support", "Smart Campus", "Library", "part-time", "80", 20, "active");
        saveJob(publisher.getId(), "Java Engineer", "Java backend", "Smart Campus", "Library", "full-time", "200", 5, "active");
        saveJob(publisher.getId(), "Python Tutor", "Python Q and A", "Smart Campus", "Library", "part-time", "90", 30, "active");
        saveJob(publisher.getId(), "Java Part Time", "Different location", "Smart Campus", "Dorm", "part-time", "90", 8, "active");
        saveJob(publisher.getId(), "Java Archived", "Closed role", "Smart Campus", "Library", "part-time", "70", 15, "closed");

        mockMvc.perform(get("/api/job/list")
                        .header("Authorization", authToken)
                        .param("keyword", "Java")
                        .param("location", "Library")
                        .param("jobType", "part-time")
                        .param("minSalary", "70")
                        .param("maxSalary", "150")
                        .param("sort", "salaryDesc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].title").value("Java Tutor"))
                .andExpect(jsonPath("$.data[1].title").value("Java Lab Assistant"));

        mockMvc.perform(get("/api/job/list")
                        .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.length()").value(5));

        mockMvc.perform(get("/api/job/search")
                        .header("Authorization", authToken)
                        .param("keyword", "Java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.length()").value(4));
    }

    @Test
    void helpListSupportsKeywordCategoryUrgencyAndSorting() throws Exception {
        User requester = userRepository.findAll().get(userRepository.findAll().size() - 1);

        saveHelp(requester.getId(), "Java Homework", "Need help tonight", "study", "high", 8, "pending");
        saveHelp(requester.getId(), "Java Lab", "Need a tutor", "study", "medium", 20, "pending");
        saveHelp(requester.getId(), "Java Delivery", "Different category", "life", "high", 30, "pending");
        saveHelp(requester.getId(), "Python Homework", "Non target", "study", "high", 50, "pending");
        saveHelp(requester.getId(), "Java Accepted", "Already claimed", "study", "high", 12, "helping");

        mockMvc.perform(get("/api/help/list")
                        .header("Authorization", authToken)
                        .param("keyword", "Java")
                        .param("category", "study")
                        .param("urgency", "high")
                        .param("sort", "viewsDesc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].title").value("Java Homework"));

        mockMvc.perform(get("/api/help/list")
                        .header("Authorization", authToken)
                        .param("keyword", "Java")
                        .param("category", "study")
                        .param("sort", "urgencyDesc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].title").value("Java Homework"))
                .andExpect(jsonPath("$.data[1].title").value("Java Lab"));

        mockMvc.perform(get("/api/help/list")
                        .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.length()").value(4));

        mockMvc.perform(get("/api/help/search")
                        .header("Authorization", authToken)
                        .param("keyword", "Java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.length()").value(3));
    }

    private void saveProduct(Long sellerId, String title, String description, String category,
                             String price, int views, String status) {
        SecondHandProduct product = new SecondHandProduct();
        product.setSellerId(sellerId);
        product.setTitle(title);
        product.setDescription(description);
        product.setCategory(category);
        product.setPrice(new BigDecimal(price));
        product.setViewCount(views);
        product.setStatus(status);
        productRepository.save(product);
    }

    private void saveJob(Long publisherId, String title, String description, String company,
                         String location, String jobType, String salary, int views, String status) {
        JobPosting job = new JobPosting();
        job.setPublisherId(publisherId);
        job.setTitle(title);
        job.setDescription(description);
        job.setCompany(company);
        job.setLocation(location);
        job.setJobType(jobType);
        job.setSalary(new BigDecimal(salary));
        job.setViewCount(views);
        job.setStatus(status);
        jobPostingRepository.save(job);
    }

    private void saveHelp(Long requesterId, String title, String description, String category,
                          String urgency, int views, String status) {
        HelpRequest helpRequest = new HelpRequest();
        helpRequest.setRequesterId(requesterId);
        helpRequest.setTitle(title);
        helpRequest.setDescription(description);
        helpRequest.setCategory(category);
        helpRequest.setUrgency(urgency);
        helpRequest.setViewCount(views);
        helpRequest.setStatus(status);
        helpRequestRepository.save(helpRequest);
    }
}
