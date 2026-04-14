package com.campus.campus_system.service;

import com.campus.campus_system.entity.JobPosting;
import com.campus.campus_system.repository.JobPostingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    // 搜索职位
    public List<JobPosting> searchJobs(String keyword) {
        return jobPostingRepository.search(keyword);
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
}

