// EcpayController.java
package com.example.springtest.controller;

import java.io.IOException;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.example.springtest.service.EcpayService;
import com.example.springtest.service.OrderService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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

    /**
     * 1. 綠界主動回傳 (ReturnURL)
     * 這是伺服器對伺服器的通訊，用來更新資料庫，使用者看不到此過程
     */
    @PostMapping("/callback")
    public String ecpayCallback(@RequestParam Map<String, String> ecpayFeedback) {
        System.out.println("📢 綠界主動回傳內容: " + ecpayFeedback.toString());

        if (!ecpayService.verifyCheckMacValue(ecpayFeedback)) {
            return "0|CheckMacValue Error";
        }

        String merchantTradeNo = ecpayFeedback.get("MerchantTradeNo");
        String rtnCode = ecpayFeedback.get("RtnCode");
        String paymentType = ecpayFeedback.get("PaymentType");
        String tradeNo = ecpayFeedback.get("TradeNo");

        try {
            orderService.updateOrderPaymentResult(merchantTradeNo, rtnCode, paymentType, tradeNo);
            return "1|OK"; // 必須回傳此字串給綠界
        } catch (Exception e) {
            return "0|Database Update Error";
        }
    }

    /**
     * 2. 使用者付完錢後導向回來的路徑 (ClientBackURL / OrderResultURL)
     * 這裡負責將使用者「轉址」回前端 Vue 的 Router 頁面
     */
    @RequestMapping(value = "/order-completed", method = { RequestMethod.GET, RequestMethod.POST })
    public void orderCompleted(
            @RequestParam Map<String, String> ecpayFeedback,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        // 偵錯用：列印出請求方法
        System.out.println("跳回通知 Method: " + request.getMethod());
        System.out.println("收到的原始 Map: " + ecpayFeedback);

        // 1. 嘗試從 Map 獲取 (適用於 POST form-data 或 GET query params)
        String merchantTradeNo = ecpayFeedback.get("MerchantTradeNo");
        String rtnCode = ecpayFeedback.get("RtnCode");

        String frontendBaseUrl = "http://localhost:5173";
        // 2. 如果還是 null，嘗試直接從 Request Parameter 獲取 (雙重保險)
        if (merchantTradeNo != null) {
            // 情況 A：有收到參數 (OrderResultURL POST 回來的)
            if ("1".equals(rtnCode)) {
                response.sendRedirect(frontendBaseUrl + "/payment/success?orderId=" + merchantTradeNo);
            } else {
                response.sendRedirect(frontendBaseUrl + "/payment/fail?orderId=" + merchantTradeNo);
            }
        } else {
            // 情況 B：沒收到參數 (可能是 ClientBackURL GET 回來的)
            // 此時資料庫其實已經被 callback 更新好了，直接導向「我的訂單」頁面即可
            System.out.println("⚠️ 未收到參數跳回，導向訂單列表");
            response.sendRedirect(frontendBaseUrl);
        }
    }
}