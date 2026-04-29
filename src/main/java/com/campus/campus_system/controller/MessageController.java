package com.campus.campus_system.controller;

import com.campus.campus_system.entity.Message;
import com.campus.campus_system.service.MessageService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.CrossOrigin;
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
@RequestMapping("/api/message")
@CrossOrigin(origins = "*")
public class MessageController {
    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping("/send")
    public Map<String, Object> sendMessage(HttpServletRequest request, @RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long currentUserId = (Long) request.getAttribute("currentUserId");
            Long senderId = params.containsKey("senderId")
                    ? Long.valueOf(params.get("senderId").toString())
                    : currentUserId;
            if (!senderId.equals(currentUserId)) {
                throw new RuntimeException("Sender does not match current user");
            }
            Long receiverId = Long.valueOf(params.get("receiverId").toString());
            String content = params.get("content").toString();
            String messageType = params.containsKey("messageType") ? params.get("messageType").toString() : "text";

            Message message = messageService.sendMessage(senderId, receiverId, content, messageType);
            result.put("code", 0);
            result.put("data", message);
        } catch (Exception e) {
            result.put("code", 1);
            result.put("msg", e.getMessage());
        }
        return result;
    }

    @GetMapping("/conversation")
    public Map<String, Object> getConversation(HttpServletRequest request,
                                               @RequestParam Long userId1,
                                               @RequestParam Long userId2) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long currentUserId = (Long) request.getAttribute("currentUserId");
            if (!currentUserId.equals(userId1) && !currentUserId.equals(userId2)) {
                throw new RuntimeException("Not allowed to view this conversation");
            }
            List<Message> messages = messageService.getConversation(userId1, userId2);
            result.put("code", 0);
            result.put("data", messages);
        } catch (Exception e) {
            result.put("code", 1);
            result.put("msg", e.getMessage());
        }
        return result;
    }

    @GetMapping("/contacts/{userId}")
    public Map<String, Object> getContacts(HttpServletRequest request, @PathVariable Long userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long currentUserId = (Long) request.getAttribute("currentUserId");
            if (!currentUserId.equals(userId)) {
                throw new RuntimeException("Not allowed to view these contacts");
            }
            List<Map<String, Object>> contacts = messageService.getContacts(userId);
            result.put("code", 0);
            result.put("data", contacts);
        } catch (Exception e) {
            result.put("code", 1);
            result.put("msg", e.getMessage());
        }
        return result;
    }

    @GetMapping("/unread/{userId}")
    public Map<String, Object> getUnreadMessages(HttpServletRequest request, @PathVariable Long userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long currentUserId = (Long) request.getAttribute("currentUserId");
            if (!currentUserId.equals(userId)) {
                throw new RuntimeException("Not allowed to view unread messages");
            }
            List<Message> messages = messageService.getUnreadMessages(userId);
            result.put("code", 0);
            result.put("data", messages);
        } catch (Exception e) {
            result.put("code", 1);
            result.put("msg", e.getMessage());
        }
        return result;
    }

    @PostMapping("/read/{messageId}")
    public Map<String, Object> markAsRead(@PathVariable Long messageId) {
        Map<String, Object> result = new HashMap<>();
        try {
            messageService.markAsRead(messageId);
            result.put("code", 0);
            result.put("msg", "Marked as read");
        } catch (Exception e) {
            result.put("code", 1);
            result.put("msg", e.getMessage());
        }
        return result;
    }
}