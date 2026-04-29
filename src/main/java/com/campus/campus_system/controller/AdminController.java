package com.campus.campus_system.controller;

import com.campus.campus_system.service.AdminService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        return ok(adminService.getStats());
    }

    @GetMapping("/users")
    public Map<String, Object> getUsers() {
        return ok(adminService.getAllUsers());
    }

    @PostMapping("/users/{id}/enable")
    public Map<String, Object> enableUser(@PathVariable Long id) {
        return ok(adminService.updateUserStatus(id, true));
    }

    @PostMapping("/users/{id}/disable")
    public Map<String, Object> disableUser(@PathVariable Long id) {
        return ok(adminService.updateUserStatus(id, false));
    }

    @GetMapping("/products")
    public Map<String, Object> getProducts() {
        return ok(adminService.getAllProducts());
    }

    @DeleteMapping("/products/{id}")
    public Map<String, Object> deleteProduct(@PathVariable Long id) {
        adminService.deleteProduct(id);
        return ok(null);
    }

    @GetMapping("/jobs")
    public Map<String, Object> getJobs() {
        return ok(adminService.getAllJobs());
    }

    @DeleteMapping("/jobs/{id}")
    public Map<String, Object> deleteJob(@PathVariable Long id) {
        adminService.deleteJob(id);
        return ok(null);
    }

    @GetMapping("/helps")
    public Map<String, Object> getHelps() {
        return ok(adminService.getAllHelps());
    }

    @DeleteMapping("/helps/{id}")
    public Map<String, Object> deleteHelp(@PathVariable Long id) {
        adminService.deleteHelp(id);
        return ok(null);
    }

    private Map<String, Object> ok(Object data) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("data", data);
        return result;
    }
}
