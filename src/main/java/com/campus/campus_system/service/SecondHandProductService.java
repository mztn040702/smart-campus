package com.campus.campus_system.service;

import com.campus.campus_system.entity.SecondHandProduct;
import com.campus.campus_system.repository.SecondHandProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class SecondHandProductService {
    @Autowired
    private SecondHandProductRepository productRepository;

    @Transactional
    public SecondHandProduct publishProduct(SecondHandProduct product) {
        return productRepository.save(product);
    }

    public List<SecondHandProduct> getAllOnSaleProducts() {
        return productRepository.findByStatus("on_sale");
    }

    public List<SecondHandProduct> getProductsByCategory(String category) {
        return productRepository.findByCategoryAndStatus(category, "on_sale");
    }

    public List<SecondHandProduct> queryProducts(String keyword, String category, BigDecimal minPrice,
                                                 BigDecimal maxPrice, String sort) {
        Specification<SecondHandProduct> specification = (root, query, criteriaBuilder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("status"), "on_sale"));

            if (keyword != null && !keyword.isBlank()) {
                String pattern = "%" + keyword.trim() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(root.get("title"), pattern),
                        criteriaBuilder.like(root.get("description"), pattern)
                ));
            }

            if (category != null && !category.isBlank()) {
                Set<String> categoryAliases = resolveCategoryAliases(category.trim());
                if (categoryAliases.size() == 1) {
                    predicates.add(criteriaBuilder.equal(root.get("category"), categoryAliases.iterator().next()));
                } else {
                    predicates.add(root.get("category").in(categoryAliases));
                }
            }

            if (minPrice != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        return productRepository.findAll(specification, resolveProductSort(sort));
    }

    public List<SecondHandProduct> searchProducts(String keyword) {
        return queryProducts(keyword, null, null, null, "latest");
    }

    public SecondHandProduct getProductById(Long id) {
        SecondHandProduct product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        product.setViewCount(product.getViewCount() + 1);
        productRepository.save(product);
        return product;
    }

    public List<SecondHandProduct> getProductsBySeller(Long sellerId) {
        return productRepository.findBySellerId(sellerId);
    }

    @Transactional
    public SecondHandProduct updateProductStatus(Long id, String status) {
        SecondHandProduct product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        product.setStatus(status);
        return productRepository.save(product);
    }

    private Sort resolveProductSort(String sort) {
        if ("priceAsc".equals(sort)) {
            return Sort.by(Sort.Direction.ASC, "price").and(Sort.by(Sort.Direction.DESC, "id"));
        }
        if ("priceDesc".equals(sort)) {
            return Sort.by(Sort.Direction.DESC, "price").and(Sort.by(Sort.Direction.DESC, "id"));
        }
        if ("viewsDesc".equals(sort)) {
            return Sort.by(Sort.Direction.DESC, "viewCount").and(Sort.by(Sort.Direction.DESC, "id"));
        }
        return Sort.by(Sort.Direction.DESC, "id");
    }

    private Set<String> resolveCategoryAliases(String category) {
        Set<String> aliases = new LinkedHashSet<>();
        aliases.add(category);

        switch (category) {
            case "books":
            case "书籍":
                aliases.add("books");
                aliases.add("书籍");
                break;
            case "electronics":
            case "电子产品":
                aliases.add("electronics");
                aliases.add("电子产品");
                break;
            case "daily":
            case "daily用品":
            case "生活用品":
                aliases.add("daily");
                aliases.add("daily用品");
                aliases.add("生活用品");
                break;
            case "other":
            case "其他":
                aliases.add("other");
                aliases.add("其他");
                break;
            default:
                break;
        }

        return aliases;
    }
}
