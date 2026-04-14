package com.campus.campus_system.repository;

import com.campus.campus_system.entity.SecondHandProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface SecondHandProductRepository extends JpaRepository<SecondHandProduct, Long> {
    // 根据状态查询
    List<SecondHandProduct> findByStatus(String status);

    // 根据分类查询
    List<SecondHandProduct> findByCategoryAndStatus(String category, String status);

    // 根据卖家ID查询
    List<SecondHandProduct> findBySellerId(Long sellerId);

    // 搜索（标题或描述包含关键词）
    @Query("SELECT p FROM SecondHandProduct p WHERE p.status = 'on_sale' AND (p.title LIKE %:keyword% OR p.description LIKE %:keyword%)")
    List<SecondHandProduct> search(@Param("keyword") String keyword);
}

