package com.example.springtest.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.springtest.DTO.OrderItemRequest;
import com.example.springtest.DTO.OrderRequest;
import com.example.springtest.model.Order;
import com.example.springtest.model.OrderItem;
import com.example.springtest.model.Product;
import com.example.springtest.model.User;
import com.example.springtest.repository.OrderRepository;
import com.example.springtest.repository.ProductRepository;
import com.example.springtest.repository.UserRepository;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Autowired
    public OrderService(OrderRepository orderRepository, UserRepository userRepository,
            ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order getOrderById(int id) {
        return orderRepository.findById(id).orElse(null);
    }

    public Order createOrder(OrderRequest orderRequest) {
        User user = userRepository.findById(orderRequest.getUserId()).orElse(null);
        Order order = new Order();
        order.setUser(user);
        // order.setTotalAmount(orderRequest.getTotalPrice());
        order.setStatus("PENDING");
        order.setOrderDate(LocalDateTime.now());

        double calculatedTotal = 0.0;
        for (OrderItemRequest itemRequest : orderRequest.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId()).orElse(null);
            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setQuantity(itemRequest.getQuantity());
            double unitPrice = product.getPrice();
            calculatedTotal += unitPrice * itemRequest.getQuantity();
            item.setPrice(unitPrice);
            item.setOrder(order);
            order.getOrderItems().add(item);
        }
        order.setTotalAmount(calculatedTotal);
        return orderRepository.save(order);
    }

    public void deleteOrder(int id) {
        orderRepository.deleteById(id);
    }

    public Order updateOrderStatus(int id, String newStatus) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + id));

        order.setStatus(newStatus);
        return orderRepository.save(order);
    }
}
