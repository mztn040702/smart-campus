package com.campus.campus_system.repository;

import com.campus.campus_system.entity.HelpRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface HelpRequestRepository extends JpaRepository<HelpRequest, Long> {
    // 根据状态查询
    List<HelpRequest> findByStatus(String status);

    // 根据分类查询
    List<HelpRequest> findByCategoryAndStatus(String category, String status);

    // 根据求助者ID查询
    List<HelpRequest> findByRequesterId(Long requesterId);

    // 根据帮助者ID查询
    List<HelpRequest> findByHelperId(Long helperId);

    // 搜索
    @Query("SELECT h FROM HelpRequest h WHERE h.status = 'pending' AND (h.title LIKE %:keyword% OR h.description LIKE %:keyword%)")
    List<HelpRequest> search(@Param("keyword") String keyword);
}

