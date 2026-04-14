package com.campus.campus_system.repository;

import com.campus.campus_system.entity.JobPosting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {
    // 根据状态查询
    List<JobPosting> findByStatus(String status);

    // 根据工作类型查询
    List<JobPosting> findByJobTypeAndStatus(String jobType, String status);

    // 根据发布者ID查询
    List<JobPosting> findByPublisherId(Long publisherId);

    // 搜索
    @Query("SELECT j FROM JobPosting j WHERE j.status = 'active' AND (j.title LIKE %:keyword% OR j.description LIKE %:keyword% OR j.company LIKE %:keyword%)")
    List<JobPosting> search(@Param("keyword") String keyword);
}

