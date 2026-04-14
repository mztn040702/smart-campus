package com.campus.campus_system.service;

import com.campus.campus_system.entity.SecondHandProduct;
import com.campus.campus_system.repository.SecondHandProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SecondHandProductService {
    @Autowired
    private SecondHandProductRepository productRepository;

    // 发布商品
    @Transactional
    public SecondHandProduct publishProduct(SecondHandProduct product) {
        return productRepository.save(product);
    }

    // 获取所有在售商品
    public List<SecondHandProduct> getAllOnSaleProducts() {
        return productRepository.findByStatus("on_sale");
    }

    // 根据分类获取商品
    public List<SecondHandProduct> getProductsByCategory(String category) {
        return productRepository.findByCategoryAndStatus(category, "on_sale");
    }

    // 搜索商品
    public List<SecondHandProduct> searchProducts(String keyword) {
        return productRepository.search(keyword);
    }

    // 获取商品详情
    public SecondHandProduct getProductById(Long id) {
        SecondHandProduct product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("商品不存在"));
        // 增加浏览次数
        product.setViewCount(product.getViewCount() + 1);
        productRepository.save(product);
        return product;
    }

    // 获取用户发布的商品
    public List<SecondHandProduct> getProductsBySeller(Long sellerId) {
        return productRepository.findBySellerId(sellerId);
    }

    // 更新商品状态
    @Transactional
    public SecondHandProduct updateProductStatus(Long id, String status) {
        SecondHandProduct product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("商品不存在"));
        product.setStatus(status);
        return productRepository.save(product);
    }
}

