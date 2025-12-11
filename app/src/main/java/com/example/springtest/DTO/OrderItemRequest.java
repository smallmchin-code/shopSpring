package com.example.springtest.DTO;

public class OrderItemRequest {
    private int productId; // 實際的 Product ID (您前端用的是 item.id)

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    private int quantity;
    private double price;
}
