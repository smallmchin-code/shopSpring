package com.example.springtest.DTO;

import java.util.List;

public class OrderRequest {
    private int userId; // 用來查詢真正的 User 物件
    private double totalPrice;
    private List<OrderItemRequest> items;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public List<OrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<OrderItemRequest> items) {
        this.items = items;
    }

}
