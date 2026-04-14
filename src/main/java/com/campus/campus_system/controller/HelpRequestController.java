package com.campus.campus_system.controller;

import com.campus.campus_system.entity.HelpRequest;
import com.campus.campus_system.service.HelpRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/help")
@CrossOrigin(origins = "*")
public class HelpRequestController {
    @Autowired
    private HelpRequestService helpRequestService;

    // 发布求助
    @PostMapping("/publish")
    public Map<String, Object> publishHelp(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            HelpRequest helpRequest = new HelpRequest();
            helpRequest.setRequesterId(Long.valueOf(params.get("requesterId").toString()));
            helpRequest.setTitle(params.get("title").toString());
            helpRequest.setDescription(params.get("description").toString());
            helpRequest.setCategory(params.get("category").toString());
            if (params.containsKey("location")) {
                helpRequest.setLocation(params.get("location").toString());
            }
            if (params.containsKey("urgency")) {
                helpRequest.setUrgency(params.get("urgency").toString());
            }
            
            HelpRequest saved = helpRequestService.publishHelpRequest(helpRequest);
            result.put("code", 0);
            result.put("data", saved);
        } catch (Exception e) {
            result.put("code", 1);
            result.put("msg", e.getMessage());
        }
        return result;
    }

    // 获取所有待帮助的请求
    @GetMapping("/list")
    public Map<String, Object> getAllRequests(@RequestParam(required = false) String category) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<HelpRequest> requests;
            if (category != null && !category.isEmpty()) {
                requests = helpRequestService.getRequestsByCategory(category);
            } else {
                requests = helpRequestService.getAllPendingRequests();
            }
            result.put("code", 0);
            result.put("data", requests);
        } catch (Exception e) {
            result.put("code", 1);
            result.put("msg", e.getMessage());
        }
        return result;
    }

    // 搜索求助
    @GetMapping("/search")
    public Map<String, Object> searchRequests(@RequestParam String keyword) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<HelpRequest> requests = helpRequestService.searchRequests(keyword);
            result.put("code", 0);
            result.put("data", requests);
        } catch (Exception e) {
            result.put("code", 1);
            result.put("msg", e.getMessage());
        }
        return result;
    }

    // 获取求助详情
    @GetMapping("/{id}")
    public Map<String, Object> getRequestById(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            HelpRequest request = helpRequestService.getRequestById(id);
            result.put("code", 0);
            result.put("data", request);
        } catch (Exception e) {
            result.put("code", 1);
            result.put("msg", e.getMessage());
        }
        return result;
    }

    // 接受帮助
    @PostMapping("/accept")
    public Map<String, Object> acceptHelp(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long requestId = Long.valueOf(params.get("requestId").toString());
            Long helperId = Long.valueOf(params.get("helperId").toString());
            HelpRequest request = helpRequestService.acceptHelp(requestId, helperId);
            result.put("code", 0);
            result.put("data", request);
        } catch (Exception e) {
            result.put("code", 1);
            result.put("msg", e.getMessage());
        }
        return result;
    }

    // 完成帮助
    @PostMapping("/complete/{id}")
    public Map<String, Object> completeHelp(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            HelpRequest request = helpRequestService.completeHelp(id);
            result.put("code", 0);
            result.put("data", request);
        } catch (Exception e) {
            result.put("code", 1);
            result.put("msg", e.getMessage());
        }
        return result;
    }
}

