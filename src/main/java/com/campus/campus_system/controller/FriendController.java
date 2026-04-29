package com.campus.campus_system.controller;

import com.campus.campus_system.entity.FriendRequest;
import com.campus.campus_system.entity.User;
import com.campus.campus_system.service.FriendService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/friends")
public class FriendController {
    private final FriendService friendService;

    public FriendController(FriendService friendService) {
        this.friendService = friendService;
    }

    @GetMapping
    public Map<String, Object> getFriends(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long currentUserId = (Long) request.getAttribute("currentUserId");
            List<User> friends = friendService.getFriends(currentUserId);
            result.put("code", 0);
            result.put("data", friends);
        } catch (Exception e) {
            result.put("code", 1);
            result.put("msg", e.getMessage());
        }
        return result;
    }

    @GetMapping("/search")
    public Map<String, Object> searchUsers(HttpServletRequest request,
                                           @RequestParam(defaultValue = "") String keyword) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long currentUserId = (Long) request.getAttribute("currentUserId");
            result.put("code", 0);
            result.put("data", friendService.searchUsers(currentUserId, keyword));
        } catch (Exception e) {
            result.put("code", 1);
            result.put("msg", e.getMessage());
        }
        return result;
    }

    @PostMapping("/request")
    public Map<String, Object> sendRequest(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long currentUserId = (Long) request.getAttribute("currentUserId");
            Long receiverId = Long.valueOf(String.valueOf(body.get("receiverId")));
            FriendRequest friendRequest = friendService.sendRequest(currentUserId, receiverId);
            result.put("code", 0);
            result.put("data", friendRequest);
        } catch (Exception e) {
            result.put("code", 1);
            result.put("msg", e.getMessage());
        }
        return result;
    }

    @GetMapping("/requests/incoming")
    public Map<String, Object> getIncomingRequests(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long currentUserId = (Long) request.getAttribute("currentUserId");
            result.put("code", 0);
            result.put("data", friendService.getIncomingRequests(currentUserId));
        } catch (Exception e) {
            result.put("code", 1);
            result.put("msg", e.getMessage());
        }
        return result;
    }

    @PostMapping("/requests/{id}/accept")
    public Map<String, Object> acceptRequest(HttpServletRequest request, @PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long currentUserId = (Long) request.getAttribute("currentUserId");
            result.put("code", 0);
            result.put("data", friendService.acceptRequest(currentUserId, id));
        } catch (Exception e) {
            result.put("code", 1);
            result.put("msg", e.getMessage());
        }
        return result;
    }

    @PostMapping("/requests/{id}/reject")
    public Map<String, Object> rejectRequest(HttpServletRequest request, @PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long currentUserId = (Long) request.getAttribute("currentUserId");
            result.put("code", 0);
            result.put("data", friendService.rejectRequest(currentUserId, id));
        } catch (Exception e) {
            result.put("code", 1);
            result.put("msg", e.getMessage());
        }
        return result;
    }
}