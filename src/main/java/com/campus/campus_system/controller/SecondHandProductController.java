package com.campus.campus_system.controller;

import com.campus.campus_system.entity.SecondHandProduct;
import com.campus.campus_system.service.SecondHandProductService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/product")
@CrossOrigin(origins = "*")
public class SecondHandProductController {
    private final SecondHandProductService productService;

    public SecondHandProductController(SecondHandProductService productService) {
        this.productService = productService;
    }

    @PostMapping("/publish")
    public Map<String, Object> publishProduct(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            SecondHandProduct product = new SecondHandProduct();
            product.setSellerId(Long.valueOf(params.get("sellerId").toString()));
            product.setTitle(params.get("title").toString());
            product.setDescription(params.get("description").toString());
            product.setPrice(new BigDecimal(params.get("price").toString()));
            product.setCategory(params.get("category").toString());
            if (params.containsKey("images")) {
                product.setImages(params.get("images").toString());
            }

            SecondHandProduct saved = productService.publishProduct(product);
            result.put("code", 0);
            result.put("data", saved);
        } catch (Exception e) {
            result.put("code", 1);
            result.put("msg", e.getMessage());
        }
        return result;
    }

    @GetMapping("/list")
    public Map<String, Object> getAllProducts(@RequestParam(required = false) String keyword,
                                              @RequestParam(required = false) String category,
                                              @RequestParam(required = false) BigDecimal minPrice,
                                              @RequestParam(required = false) BigDecimal maxPrice,
                                              @RequestParam(required = false, defaultValue = "latest") String sort) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<SecondHandProduct> products = productService.queryProducts(
                    keyword, category, minPrice, maxPrice, sort
            );
            result.put("code", 0);
            result.put("data", products);
        } catch (Exception e) {
            result.put("code", 1);
            result.put("msg", e.getMessage());
        }
        return result;
    }

    @GetMapping("/search")
    public Map<String, Object> searchProducts(@RequestParam String keyword) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<SecondHandProduct> products = productService.searchProducts(keyword);
            result.put("code", 0);
            result.put("data", products);
        } catch (Exception e) {
            result.put("code", 1);
            result.put("msg", e.getMessage());
        }
        return result;
    }

    @GetMapping("/{id}")
    public Map<String, Object> getProductById(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            SecondHandProduct product = productService.getProductById(id);
            result.put("code", 0);
            result.put("data", product);
        } catch (Exception e) {
            result.put("code", 1);
            result.put("msg", e.getMessage());
        }
        return result;
    }

    @GetMapping("/my/{sellerId}")
    public Map<String, Object> getMyProducts(@PathVariable Long sellerId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<SecondHandProduct> products = productService.getProductsBySeller(sellerId);
            result.put("code", 0);
            result.put("data", products);
        } catch (Exception e) {
            result.put("code", 1);
            result.put("msg", e.getMessage());
        }
        return result;
    }
}
