package com.campus.campus_system.service;

import com.campus.campus_system.entity.HelpRequest;
import com.campus.campus_system.entity.JobPosting;
import com.campus.campus_system.entity.SecondHandProduct;
import com.campus.campus_system.entity.User;
import com.campus.campus_system.repository.HelpRequestRepository;
import com.campus.campus_system.repository.JobPostingRepository;
import com.campus.campus_system.repository.MessageRepository;
import com.campus.campus_system.repository.SecondHandProductRepository;
import com.campus.campus_system.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {
    private final UserRepository userRepository;
    private final SecondHandProductRepository productRepository;
    private final JobPostingRepository jobPostingRepository;
    private final HelpRequestRepository helpRequestRepository;
    private final MessageRepository messageRepository;

    public AdminService(
            UserRepository userRepository,
            SecondHandProductRepository productRepository,
            JobPostingRepository jobPostingRepository,
            HelpRequestRepository helpRequestRepository,
            MessageRepository messageRepository
    ) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.jobPostingRepository = jobPostingRepository;
        this.helpRequestRepository = helpRequestRepository;
        this.messageRepository = messageRepository;
    }

    public Map<String, Long> getStats() {
        Map<String, Long> stats = new LinkedHashMap<>();
        stats.put("userCount", userRepository.count());
        stats.put("productCount", productRepository.count());
        stats.put("jobCount", jobPostingRepository.count());
        stats.put("helpCount", helpRequestRepository.count());
        stats.put("messageCount", messageRepository.count());
        return stats;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public User updateUserStatus(Long id, boolean enabled) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setStatus(enabled ? "ACTIVE" : "DISABLED");
        return userRepository.save(user);
    }

    public List<SecondHandProduct> getAllProducts() {
        return productRepository.findAll();
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found");
        }
        productRepository.deleteById(id);
    }

    public List<JobPosting> getAllJobs() {
        return jobPostingRepository.findAll();
    }

    @Transactional
    public void deleteJob(Long id) {
        if (!jobPostingRepository.existsById(id)) {
            throw new RuntimeException("Job not found");
        }
        jobPostingRepository.deleteById(id);
    }

    public List<HelpRequest> getAllHelps() {
        return helpRequestRepository.findAll();
    }

    @Transactional
    public void deleteHelp(Long id) {
        if (!helpRequestRepository.existsById(id)) {
            throw new RuntimeException("Help not found");
        }
        helpRequestRepository.deleteById(id);
    }
}
