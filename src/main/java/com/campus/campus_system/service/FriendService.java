package com.campus.campus_system.service;

import com.campus.campus_system.entity.FriendRequest;
import com.campus.campus_system.entity.FriendRequestStatus;
import com.campus.campus_system.entity.User;
import com.campus.campus_system.repository.FriendRequestRepository;
import com.campus.campus_system.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FriendService {
    private final FriendRequestRepository friendRequestRepository;
    private final UserRepository userRepository;

    public FriendService(FriendRequestRepository friendRequestRepository, UserRepository userRepository) {
        this.friendRequestRepository = friendRequestRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public FriendRequest sendRequest(Long requesterId, Long receiverId) {
        if (requesterId.equals(receiverId)) {
            throw new RuntimeException("Cannot add yourself as a friend");
        }
        userRepository.findById(receiverId).orElseThrow(() -> new RuntimeException("User not found"));
        if (areFriends(requesterId, receiverId)) {
            throw new RuntimeException("Users are already friends");
        }
        if (friendRequestRepository.findByRequesterIdAndReceiverIdAndStatus(
                requesterId, receiverId, FriendRequestStatus.PENDING
        ).isPresent()) {
            throw new RuntimeException("Friend request already sent");
        }

        FriendRequest request = new FriendRequest();
        request.setRequesterId(requesterId);
        request.setReceiverId(receiverId);
        request.setStatus(FriendRequestStatus.PENDING);
        return friendRequestRepository.save(request);
    }

    @Transactional
    public FriendRequest acceptRequest(Long currentUserId, Long requestId) {
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Friend request not found"));
        if (!request.getReceiverId().equals(currentUserId)) {
            throw new RuntimeException("Not allowed to accept this request");
        }
        request.setStatus(FriendRequestStatus.ACCEPTED);
        return friendRequestRepository.save(request);
    }

    @Transactional
    public FriendRequest rejectRequest(Long currentUserId, Long requestId) {
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Friend request not found"));
        if (!request.getReceiverId().equals(currentUserId)) {
            throw new RuntimeException("Not allowed to reject this request");
        }
        request.setStatus(FriendRequestStatus.REJECTED);
        return friendRequestRepository.save(request);
    }

    public List<Map<String, Object>> getIncomingRequests(Long currentUserId) {
        return friendRequestRepository.findByReceiverIdAndStatusOrderByCreateTimeDesc(currentUserId, FriendRequestStatus.PENDING)
                .stream()
                .map(request -> {
                    User requester = userRepository.findById(request.getRequesterId())
                            .orElseThrow(() -> new RuntimeException("User not found"));
                    Map<String, Object> data = new HashMap<>();
                    data.put("id", request.getId());
                    data.put("requesterId", requester.getId());
                    data.put("username", requester.getUsername());
                    data.put("realName", requester.getRealName());
                    data.put("avatar", requester.getAvatar());
                    data.put("status", request.getStatus());
                    data.put("createTime", request.getCreateTime());
                    return data;
                })
                .toList();
    }

    public List<User> getFriends(Long currentUserId) {
        List<FriendRequest> relationships = friendRequestRepository.findRelationshipsForUser(
                currentUserId, FriendRequestStatus.ACCEPTED
        );
        Set<Long> friendIds = relationships.stream()
                .map(request -> request.getRequesterId().equals(currentUserId) ? request.getReceiverId() : request.getRequesterId())
                .collect(Collectors.toSet());
        return userRepository.findAllById(friendIds);
    }

    public List<User> searchUsers(Long currentUserId, String keyword) {
        String normalized = keyword == null ? "" : keyword.trim().toLowerCase();
        Set<Long> friendIds = getFriends(currentUserId).stream().map(User::getId).collect(Collectors.toSet());
        return userRepository.findAll().stream()
                .filter(user -> !user.getId().equals(currentUserId))
                .filter(user -> !friendIds.contains(user.getId()))
                .filter(user -> normalized.isEmpty()
                        || user.getUsername().toLowerCase().contains(normalized)
                        || (user.getRealName() != null && user.getRealName().toLowerCase().contains(normalized)))
                .toList();
    }

    public boolean areFriends(Long userId1, Long userId2) {
        return friendRequestRepository.existsRelationship(userId1, userId2, FriendRequestStatus.ACCEPTED);
    }
}