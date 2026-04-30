package com.campus.campus_system.repository;

import com.campus.campus_system.entity.HelpRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HelpRequestRepository extends JpaRepository<HelpRequest, Long>, JpaSpecificationExecutor<HelpRequest> {
    List<HelpRequest> findByStatus(String status);

    List<HelpRequest> findByCategoryAndStatus(String category, String status);

    List<HelpRequest> findByRequesterId(Long requesterId);

    List<HelpRequest> findByHelperId(Long helperId);

    @Query("SELECT h FROM HelpRequest h WHERE h.status = 'pending' AND (h.title LIKE %:keyword% OR h.description LIKE %:keyword%)")
    List<HelpRequest> search(@Param("keyword") String keyword);
}
