package com.campus.campus_system.repository;

import com.campus.campus_system.entity.SecondHandProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SecondHandProductRepository extends JpaRepository<SecondHandProduct, Long> {
    List<SecondHandProduct> findByStatus(String status);

    List<SecondHandProduct> findByCategoryAndStatus(String category, String status);

    List<SecondHandProduct> findBySellerId(Long sellerId);

    @Query("SELECT p FROM SecondHandProduct p WHERE p.status = 'on_sale' AND (p.title LIKE %:keyword% OR p.description LIKE %:keyword%)")
    List<SecondHandProduct> search(@Param("keyword") String keyword);
}