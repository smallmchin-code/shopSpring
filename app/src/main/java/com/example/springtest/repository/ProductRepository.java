package com.example.springtest.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.springtest.model.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.variants v")
    List<Product> findAllWithVariants();

    // 2. 分類頁修正：依分類載入商品時，同時載入變體 (Variants)
    // 替換您原有的 findByCategory(String category)
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.variants v WHERE p.category = :category")
    List<Product> findByCategoryWithVariants(@Param("category") String category);

    // 3. 細節頁修正：確保圖片和變體都載入 (防止單一商品頁出現 500 錯誤)
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.variants v WHERE p.id = :id")
    Optional<Product> findByIdWithVariants(@Param("id") int id);
}
