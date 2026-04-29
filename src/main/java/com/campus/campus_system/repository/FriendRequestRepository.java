package com.campus.campus_system.repository;

import com.campus.campus_system.entity.FriendRequest;
import com.campus.campus_system.entity.FriendRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {
    List<FriendRequest> findByReceiverIdAndStatusOrderByCreateTimeDesc(Long receiverId, FriendRequestStatus status);

    Optional<FriendRequest> findByRequesterIdAndReceiverIdAndStatus(Long requesterId, Long receiverId, FriendRequestStatus status);

    @Query("""
            select case when count(fr) > 0 then true else false end
            from FriendRequest fr
            where ((fr.requesterId = :userId1 and fr.receiverId = :userId2)
                or (fr.requesterId = :userId2 and fr.receiverId = :userId1))
              and fr.status = :status
            """)
    boolean existsRelationship(@Param("userId1") Long userId1,
                               @Param("userId2") Long userId2,
                               @Param("status") FriendRequestStatus status);

    @Query("""
            select fr from FriendRequest fr
            where (fr.requesterId = :userId or fr.receiverId = :userId)
              and fr.status = :status
            order by fr.updateTime desc
            """)
    List<FriendRequest> findRelationshipsForUser(@Param("userId") Long userId,
                                                 @Param("status") FriendRequestStatus status);
}