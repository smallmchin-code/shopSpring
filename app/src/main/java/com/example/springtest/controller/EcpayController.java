// EcpayController.java
package com.example.springtest.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.springtest.service.EcpayService;
import com.example.springtest.service.OrderService;

@RestController
@RequestMapping("/api/ecpay")
public class EcpayController {

    private final OrderService orderService;
    private final EcpayService ecpayService;

    @Autowired
    public EcpayController(OrderService orderService, EcpayService ecpayService) {
        this.orderService = orderService;
        this.ecpayService = ecpayService;
    }

    // 綠界交易完成後，會 POST 數據到這個端點 (ReturnURL)
    // 綠界要求回傳純文字 "1|OK" 表示接收成功
    @PostMapping("/callback")
    public String ecpayCallback(@RequestParam Map<String, String> ecpayFeedback) {
        System.out.println("📢 綠界主動回傳內容: " + ecpayFeedback.toString());
        // 1. **驗證 CheckMacValue**
        if (!ecpayService.verifyCheckMacValue(ecpayFeedback)) {
            System.err.println("❌ 綠界 CheckMacValue 驗證失敗!");
            return "0|CheckMacValue Error";
        }

        // 2. **獲取重要參數**
        String merchantTradeNo = ecpayFeedback.get("MerchantTradeNo"); // 您的訂單編號
        String rtnCode = ecpayFeedback.get("RtnCode"); // 交易狀態碼 (1 = 成功)
        String paymentType = ecpayFeedback.get("PaymentType"); // 付款方式
        String tradeNo = ecpayFeedback.get("TradeNo"); // 綠界交易序號

        try {
            // 3. **更新訂單狀態**
            orderService.updateOrderPaymentResult(merchantTradeNo, rtnCode, paymentType, tradeNo);
        } catch (Exception e) {
            System.err.println("❌ 訂單更新失敗: " + e.getMessage());
            // 處理資料庫錯誤，回傳 0|Error 讓綠界重送通知 (如果有的話)
            e.printStackTrace();
            return "0|Database Update Error";
        }

        // 4. **成功回傳給綠界的回應**
        return "1|OK";
    }
}