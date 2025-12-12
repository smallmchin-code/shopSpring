package com.example.springtest.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.example.springtest.DTO.OrderRequest;
import com.example.springtest.model.Order;
import com.example.springtest.service.OrderService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping("/myorders")
    public List<Order> getMyOrders(HttpSession session) { // 💡 從 Session 獲取 ID
        Integer userId = (Integer) session.getAttribute("userId");

        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not logged in");
        }

        // 假設 OrderService 中有此方法
        return orderService.getOrdersByUserId(userId);
    }

    @PostMapping
    public Order createOrder(@RequestBody OrderRequest orderRequest) {
        return orderService.createOrder(orderRequest);
    }

    @DeleteMapping("/{id}")
    public void deleteOrder(@PathVariable int id) {
        orderService.deleteOrder(id);
    }

    @PutMapping("/{id}/status")
    public Order updateOrderStatus(@PathVariable int id, @RequestBody String status) {
        return orderService.updateOrderStatus(id, status); // 💡 需要在 OrderService 中新增這個方法
    }
}
