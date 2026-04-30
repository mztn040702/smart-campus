package com.campus.campus_system.repository;

import com.campus.campus_system.entity.UserPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserPreferenceRepository extends JpaRepository<UserPreference, Long> {
    List<UserPreference> findByUserId(Long userId);

    List<UserPreference> findByUserIdAndCategory(Long userId, String category);

    @Query("SELECT p FROM UserPreference p WHERE p.userId = :userId ORDER BY p.clickCount DESC, p.lastClickTime DESC")
    List<UserPreference> findTopPreferencesByUserId(@Param("userId") Long userId);
}