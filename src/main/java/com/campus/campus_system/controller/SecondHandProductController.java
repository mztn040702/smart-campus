package com.campus.campus_system.controller;

import com.campus.campus_system.entity.SecondHandProduct;
import com.campus.campus_system.service.SecondHandProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/product")
@CrossOrigin(origins = "*")
public class SecondHandProductController {
    @Autowired
    private SecondHandProductService productService;

    // 发布商品
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

    // 获取所有在售商品
    @GetMapping("/list")
    public Map<String, Object> getAllProducts(@RequestParam(required = false) String category) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<SecondHandProduct> products;
            if (category != null && !category.isEmpty()) {
                products = productService.getProductsByCategory(category);
            } else {
                products = productService.getAllOnSaleProducts();
            }
            result.put("code", 0);
            result.put("data", products);
        } catch (Exception e) {
            result.put("code", 1);
            result.put("msg", e.getMessage());
        }
        return result;
    }

    // 搜索商品
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

    // 获取商品详情
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

    // 获取用户发布的商品
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

