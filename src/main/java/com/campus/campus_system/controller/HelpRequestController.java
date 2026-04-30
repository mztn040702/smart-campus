package com.campus.campus_system.controller;

import com.campus.campus_system.entity.HelpRequest;
import com.campus.campus_system.service.HelpRequestService;
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
@RequestMapping("/api/help")
@CrossOrigin(origins = "*")
public class HelpRequestController {
    private final HelpRequestService helpRequestService;

    public HelpRequestController(HelpRequestService helpRequestService) {
        this.helpRequestService = helpRequestService;
    }

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

    @GetMapping("/list")
    public Map<String, Object> getAllRequests(@RequestParam(required = false) String keyword,
                                              @RequestParam(required = false) String category,
                                              @RequestParam(required = false) String urgency,
                                              @RequestParam(required = false, defaultValue = "latest") String sort) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<HelpRequest> requests = helpRequestService.queryRequests(
                    keyword, category, urgency, sort
            );
            result.put("code", 0);
            result.put("data", requests);
        } catch (Exception e) {
            result.put("code", 1);
            result.put("msg", e.getMessage());
        }
        return result;
    }

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
