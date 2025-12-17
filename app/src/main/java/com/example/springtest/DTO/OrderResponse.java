package com.example.springtest.DTO;

import java.util.Map;

public class OrderResponse {
    private int orderId;
    private double amount;
    private String merchantTradeNo;
    private String ecpayApiUrl;
    private Map<String, String> ecpayParams;

    public OrderResponse(int orderId, double amount, String merchantTradeNo, String ecpayApiUrl,
            Map<String, String> ecpayParams) {
        this.orderId = orderId;
        this.amount = amount;
        this.merchantTradeNo = merchantTradeNo;
        this.ecpayApiUrl = ecpayApiUrl;
        this.ecpayParams = ecpayParams;
    }

    // getters & setters
    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getMerchantTradeNo() {
        return merchantTradeNo;
    }

    public void setMerchantTradeNo(String merchantTradeNo) {
        this.merchantTradeNo = merchantTradeNo;
    }

    public String getEcpayApiUrl() {
        return ecpayApiUrl;
    }

    public void setEcpayApiUrl(String ecpayApiUrl) {
        this.ecpayApiUrl = ecpayApiUrl;
    }

    public Map<String, String> getEcpayParams() {
        return ecpayParams;
    }

    public void setEcpayParams(Map<String, String> ecpayParams) {
        this.ecpayParams = ecpayParams;
    }
}
