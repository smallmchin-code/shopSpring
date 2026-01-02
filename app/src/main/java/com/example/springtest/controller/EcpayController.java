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

    @RequestMapping(value = "/order-completed", method = { RequestMethod.GET, RequestMethod.POST })
    public void orderCompleted(
            @RequestParam Map<String, String> ecpayFeedback,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        System.out.println("跳回通知 Method: " + request.getMethod());
        System.out.println("收到的原始 Map: " + ecpayFeedback);

        String merchantTradeNo = ecpayFeedback.get("MerchantTradeNo");
        String rtnCode = ecpayFeedback.get("RtnCode");

        String frontendBaseUrl = "http://localhost:5173";
        if (merchantTradeNo != null) {
            if ("1".equals(rtnCode)) {
                response.sendRedirect(frontendBaseUrl + "/payment/success?orderId=" + merchantTradeNo);
            } else {
                response.sendRedirect(frontendBaseUrl + "/payment/fail?orderId=" + merchantTradeNo);
            }
        } else {
            System.out.println("⚠️ 未收到參數跳回，導向訂單列表");
            response.sendRedirect(frontendBaseUrl);
        }
    }
}