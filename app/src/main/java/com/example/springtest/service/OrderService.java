package com.example.springtest.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.springtest.DTO.OrderItemRequest;
import com.example.springtest.DTO.OrderRequest;
import com.example.springtest.DTO.OrderResponse;
import com.example.springtest.model.Order;
import com.example.springtest.model.OrderItem;
import com.example.springtest.model.Product;
import com.example.springtest.model.ProductVariant;
import com.example.springtest.model.User;
import com.example.springtest.repository.OrderRepository;
import com.example.springtest.repository.ProductRepository;
import com.example.springtest.repository.UserRepository;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final EcpayService ecpayService;

    @Autowired
    public OrderService(OrderRepository orderRepository, UserRepository userRepository,
            ProductRepository productRepository, EcpayService ecpayService) { // 💡 新增注入
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.ecpayService = ecpayService; // 💡 賦值
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order getOrderById(int id) {
        return orderRepository.findById(id).orElse(null);
    }

    public List<Order> getOrdersByUserId(int userId) {
        return orderRepository.findByUserId(userId);
    }

    @Transactional
    public OrderResponse createOrder(OrderRequest orderRequest) {
        User user = userRepository.findById(orderRequest.getUserId()).orElse(null);

        Order order = new Order();
        order.setUser(user);
        order.setStatus("PENDING");
        order.setOrderDate(LocalDateTime.now());
        order.setPaymentStatus("UNPAID"); // 預設未付款
        order.setPaymentMethod(null); // 付款方式尚未選擇
        order.setTradeNo(null); // 尚未有綠界交易編號
        order.setPaymentTime(null); // 尚未付款

        double calculatedTotal = 0.0;
        for (OrderItemRequest itemRequest : orderRequest.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new RuntimeException("商品不存在"));
            int variantId = itemRequest.getVariantId();
            if (variantId == 0) {
                throw new RuntimeException("商品 " + product.getName() + " 缺少規格資訊 (variantId=0)");
            }
            ProductVariant variant = product.getVariants().stream()
                    .filter(v -> v.getId() == itemRequest.getVariantId())
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("商品 " + product.getName() + " 找不到規格 ID: " + variantId +
                            "，可用規格: " + product.getVariants().stream()
                                    .map(v -> String.valueOf(v.getId()))
                                    .collect(Collectors.joining(", "))));
            if (variant.getStock() < itemRequest.getQuantity()) {
                throw new RuntimeException("商品 " + product.getName() + " 庫存不足！");
            }
            variant.setStock(variant.getStock() - itemRequest.getQuantity());

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
        order = orderRepository.save(order);

        OrderResponse orderResponse = ecpayService.createPaymentRequest(order);

        // 💡 記得要將 EcpayService 中生成的 TradeNo 存回資料庫
        // 因為 createPaymentRequest 已經修改了 order 實體的 tradeNo，所以需要再次儲存
        order.setMerchantTradeNo(orderResponse.getMerchantTradeNo());
        orderRepository.save(order);

        // ===== 回傳前端 =====
        return orderResponse;
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

    @Transactional
    public void updateOrderPaymentResult(String merchantTradeNo, String rtnCode, String paymentType, String tradeNo) {
        // 根據我們自己生成的 MerchantTradeNo 尋找訂單
        Order order = orderRepository.findByMerchantTradeNo(merchantTradeNo);

        if (order != null && "1".equals(rtnCode)) {
            order.setPaymentStatus("PAID");
            order.setStatus("PROCESSING");
            order.setPaymentMethod(paymentType);
            order.setTradeNo(tradeNo); // 這裡存的是綠界回傳的 251217... 那串長數字
            order.setPaymentTime(LocalDateTime.now());
            orderRepository.save(order);
            System.out.println("✅ 訂單 " + merchantTradeNo + " 已成功更新為 PAID");
        } else {
            // 交易失敗或處理中，僅更新狀態
            order.setPaymentStatus("FAILED");
            order.setStatus("CANCELLED");
        }

        orderRepository.save(order);
    }

}
