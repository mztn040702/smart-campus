package com.campus.campus_system.service;

import com.campus.campus_system.entity.JobPosting;
import com.campus.campus_system.repository.JobPostingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class JobPostingService {
    @Autowired
    private JobPostingRepository jobPostingRepository;

    // 发布求职信息
    @Transactional
    public JobPosting publishJob(JobPosting jobPosting) {
        return jobPostingRepository.save(jobPosting);
    }

    // 获取所有有效职位
    public List<JobPosting> getAllActiveJobs() {
        return jobPostingRepository.findByStatus("active");
    }

    // 根据工作类型获取职位
    public List<JobPosting> getJobsByType(String jobType) {
        return jobPostingRepository.findByJobTypeAndStatus(jobType, "active");
    }

    public List<JobPosting> queryJobs(String keyword, String location, String jobType, BigDecimal minSalary,
                                      BigDecimal maxSalary, String sort) {
        Specification<JobPosting> specification = (root, query, criteriaBuilder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("status"), "active"));

            if (keyword != null && !keyword.isBlank()) {
                String pattern = "%" + keyword.trim() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(root.get("title"), pattern),
                        criteriaBuilder.like(root.get("description"), pattern),
                        criteriaBuilder.like(root.get("company"), pattern)
                ));
            }

            if (location != null && !location.isBlank()) {
                String pattern = "%" + location.trim() + "%";
                predicates.add(criteriaBuilder.like(root.get("location"), pattern));
            }

            if (jobType != null && !jobType.isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("jobType"), jobType.trim()));
            }

            if (minSalary != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("salary"), minSalary));
            }

            if (maxSalary != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("salary"), maxSalary));
            }

            return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        return jobPostingRepository.findAll(specification, resolveJobSort(sort));
    }

    // 搜索职位
    public List<JobPosting> searchJobs(String keyword) {
        return queryJobs(keyword, null, null, null, null, "latest");
    }

    // 获取职位详情
    public JobPosting getJobById(Long id) {
        JobPosting job = jobPostingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("职位不存在"));
        // 增加浏览次数
        job.setViewCount(job.getViewCount() + 1);
        jobPostingRepository.save(job);
        return job;
    }

    // 获取用户发布的职位
    public List<JobPosting> getJobsByPublisher(Long publisherId) {
        return jobPostingRepository.findByPublisherId(publisherId);
    }

    // 更新职位状态
    @Transactional
    public JobPosting updateJobStatus(Long id, String status) {
        JobPosting job = jobPostingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("职位不存在"));
        job.setStatus(status);
        return jobPostingRepository.save(job);
    }

    private Sort resolveJobSort(String sort) {
        if ("salaryAsc".equals(sort)) {
            return Sort.by(Sort.Direction.ASC, "salary").and(Sort.by(Sort.Direction.DESC, "id"));
        }
        if ("salaryDesc".equals(sort)) {
            return Sort.by(Sort.Direction.DESC, "salary").and(Sort.by(Sort.Direction.DESC, "id"));
        }
        if ("viewsDesc".equals(sort)) {
            return Sort.by(Sort.Direction.DESC, "viewCount").and(Sort.by(Sort.Direction.DESC, "id"));
        }
        return Sort.by(Sort.Direction.DESC, "id");
    }
}

