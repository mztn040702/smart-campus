package com.campus.campus_system.controller;

import com.campus.campus_system.entity.JobPosting;
import com.campus.campus_system.service.JobPostingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/job")
@CrossOrigin(origins = "*")
public class JobPostingController {
    @Autowired
    private JobPostingService jobPostingService;

    // 发布职位
    @PostMapping("/publish")
    public Map<String, Object> publishJob(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            JobPosting job = new JobPosting();
            job.setPublisherId(Long.valueOf(params.get("publisherId").toString()));
            job.setTitle(params.get("title").toString());
            job.setDescription(params.get("description").toString());
            job.setCompany(params.get("company").toString());
            job.setLocation(params.get("location").toString());
            job.setJobType(params.get("jobType").toString());
            if (params.containsKey("salary")) {
                job.setSalary(new BigDecimal(params.get("salary").toString()));
            }
            if (params.containsKey("requirements")) {
                job.setRequirements(params.get("requirements").toString());
            }
            if (params.containsKey("contact")) {
                job.setContact(params.get("contact").toString());
            }
            
            JobPosting saved = jobPostingService.publishJob(job);
            result.put("code", 0);
            result.put("data", saved);
        } catch (Exception e) {
            result.put("code", 1);
            result.put("msg", e.getMessage());
        }
        return result;
    }

    // 获取所有有效职位
    @GetMapping("/list")
    public Map<String, Object> getAllJobs(@RequestParam(required = false) String jobType) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<JobPosting> jobs;
            if (jobType != null && !jobType.isEmpty()) {
                jobs = jobPostingService.getJobsByType(jobType);
            } else {
                jobs = jobPostingService.getAllActiveJobs();
            }
            result.put("code", 0);
            result.put("data", jobs);
        } catch (Exception e) {
            result.put("code", 1);
            result.put("msg", e.getMessage());
        }
        return result;
    }

    // 搜索职位
    @GetMapping("/search")
    public Map<String, Object> searchJobs(@RequestParam String keyword) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<JobPosting> jobs = jobPostingService.searchJobs(keyword);
            result.put("code", 0);
            result.put("data", jobs);
        } catch (Exception e) {
            result.put("code", 1);
            result.put("msg", e.getMessage());
        }
        return result;
    }

    // 获取职位详情
    @GetMapping("/{id}")
    public Map<String, Object> getJobById(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            JobPosting job = jobPostingService.getJobById(id);
            result.put("code", 0);
            result.put("data", job);
        } catch (Exception e) {
            result.put("code", 1);
            result.put("msg", e.getMessage());
        }
        return result;
    }

    // 获取用户发布的职位
    @GetMapping("/my/{publisherId}")
    public Map<String, Object> getMyJobs(@PathVariable Long publisherId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<JobPosting> jobs = jobPostingService.getJobsByPublisher(publisherId);
            result.put("code", 0);
            result.put("data", jobs);
        } catch (Exception e) {
            result.put("code", 1);
            result.put("msg", e.getMessage());
        }
        return result;
    }
}

