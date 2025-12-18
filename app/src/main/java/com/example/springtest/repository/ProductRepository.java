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

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.variants v WHERE p.category = :category")
    List<Product> findByCategoryWithVariants(@Param("category") String category);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.variants v WHERE p.id = :id")
    Optional<Product> findByIdWithVariants(@Param("id") int id);

}
