package com.campus.campus_system.service;

import com.campus.campus_system.entity.Message;
import com.campus.campus_system.entity.User;
import com.campus.campus_system.repository.MessageRepository;
import com.campus.campus_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MessageService {
    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    // 发送消息
    @Transactional
    public Message sendMessage(Long senderId, Long receiverId, String content, String messageType) {
        Message message = new Message();
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(content);
        message.setMessageType(messageType != null ? messageType : "text");
        message.setIsRead(false);
        return messageRepository.save(message);
    }

    // 获取两个用户之间的对话
    public List<Message> getConversation(Long userId1, Long userId2) {
        return messageRepository.findConversation(userId1, userId2);
    }

    // 获取未读消息
    public List<Message> getUnreadMessages(Long userId) {
        return messageRepository.findByReceiverIdAndIsReadFalse(userId);
    }

    // 标记消息为已读
    @Transactional
    public void markAsRead(Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("消息不存在"));
        message.setIsRead(true);
        messageRepository.save(message);
    }

    // 获取用户的联系人列表（带最后一条消息）
    public List<Map<String, Object>> getContacts(Long userId) {
        List<Long> contactUserIds = messageRepository.findContactUserIds(userId);
        return contactUserIds.stream().map(contactUserId -> {
            User contact = userRepository.findById(contactUserId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            List<Message> messages = messageRepository.findConversation(userId, contactUserId);
            Message lastMessage = messages.isEmpty() ? null : messages.get(messages.size() - 1);
            
            Map<String, Object> contactInfo = new java.util.HashMap<>();
            contactInfo.put("userId", contact.getId());
            contactInfo.put("username", contact.getUsername());
            contactInfo.put("realName", contact.getRealName());
            contactInfo.put("avatar", contact.getAvatar());
            contactInfo.put("lastMessage", lastMessage);
            contactInfo.put("unreadCount", messageRepository.findByReceiverIdAndIsReadFalse(userId).stream()
                    .filter(m -> m.getSenderId().equals(contactUserId))
                    .count());
            return contactInfo;
        }).collect(Collectors.toList());
    }
}

