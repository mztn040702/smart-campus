package com.campus.campus_system.repository;

import com.campus.campus_system.entity.UserBehavior;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserBehaviorRepository extends JpaRepository<UserBehavior, Long> {
    @Query("""
            SELECT b FROM UserBehavior b
            WHERE b.userId = :userId AND b.category = :category
            ORDER BY b.behaviorTime DESC, b.id DESC
            """)
    List<UserBehavior> findRecentByUserIdAndCategory(@Param("userId") Long userId, @Param("category") String category);
}
