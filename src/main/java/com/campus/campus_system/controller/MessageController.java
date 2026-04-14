package com.campus.campus_system.controller;

import com.campus.campus_system.entity.Message;
import com.campus.campus_system.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/message")
@CrossOrigin(origins = "*")
public class MessageController {
    @Autowired
    private MessageService messageService;

    // 发送消息
    @PostMapping("/send")
    public Map<String, Object> sendMessage(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long senderId = Long.valueOf(params.get("senderId").toString());
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

    // 获取对话
    @GetMapping("/conversation")
    public Map<String, Object> getConversation(@RequestParam Long userId1, @RequestParam Long userId2) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Message> messages = messageService.getConversation(userId1, userId2);
            result.put("code", 0);
            result.put("data", messages);
        } catch (Exception e) {
            result.put("code", 1);
            result.put("msg", e.getMessage());
        }
        return result;
    }

    // 获取联系人列表
    @GetMapping("/contacts/{userId}")
    public Map<String, Object> getContacts(@PathVariable Long userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Map<String, Object>> contacts = messageService.getContacts(userId);
            result.put("code", 0);
            result.put("data", contacts);
        } catch (Exception e) {
            result.put("code", 1);
            result.put("msg", e.getMessage());
        }
        return result;
    }

    // 获取未读消息
    @GetMapping("/unread/{userId}")
    public Map<String, Object> getUnreadMessages(@PathVariable Long userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Message> messages = messageService.getUnreadMessages(userId);
            result.put("code", 0);
            result.put("data", messages);
        } catch (Exception e) {
            result.put("code", 1);
            result.put("msg", e.getMessage());
        }
        return result;
    }

    // 标记消息为已读
    @PostMapping("/read/{messageId}")
    public Map<String, Object> markAsRead(@PathVariable Long messageId) {
        Map<String, Object> result = new HashMap<>();
        try {
            messageService.markAsRead(messageId);
            result.put("code", 0);
            result.put("msg", "标记成功");
        } catch (Exception e) {
            result.put("code", 1);
            result.put("msg", e.getMessage());
        }
        return result;
    }
}

