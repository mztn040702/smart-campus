package com.campus.campus_system.service;

import com.campus.campus_system.entity.Message;
import com.campus.campus_system.entity.User;
import com.campus.campus_system.repository.MessageRepository;
import com.campus.campus_system.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MessageService {
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final FriendService friendService;

    public MessageService(MessageRepository messageRepository,
                          UserRepository userRepository,
                          FriendService friendService) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.friendService = friendService;
    }

    @Transactional
    public Message sendMessage(Long senderId, Long receiverId, String content, String messageType) {
        ensureFriends(senderId, receiverId);

        Message message = new Message();
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(content);
        message.setMessageType(messageType != null ? messageType : "text");
        message.setIsRead(false);
        return messageRepository.save(message);
    }

    public List<Message> getConversation(Long userId1, Long userId2) {
        ensureFriends(userId1, userId2);
        return messageRepository.findConversation(userId1, userId2);
    }

    public List<Message> getUnreadMessages(Long userId) {
        return messageRepository.findByReceiverIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAsRead(Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        message.setIsRead(true);
        messageRepository.save(message);
    }

    public List<Map<String, Object>> getContacts(Long userId) {
        List<Long> contactUserIds = friendService.getFriends(userId).stream()
                .map(User::getId)
                .toList();

        return contactUserIds.stream().map(contactUserId -> {
            User contact = userRepository.findById(contactUserId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
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

    private void ensureFriends(Long userId1, Long userId2) {
        if (!friendService.areFriends(userId1, userId2)) {
            throw new RuntimeException("Users are not friends");
        }
    }
}