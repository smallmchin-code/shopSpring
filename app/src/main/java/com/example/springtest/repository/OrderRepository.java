package com.example.springtest.repository;

import com.example.springtest.model.Order;

import java.util.List;

import org.aspectj.weaver.ast.Or;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findByUserId(int userId);

    Order findByMerchantTradeNo(String merchantTradeNo);
}
