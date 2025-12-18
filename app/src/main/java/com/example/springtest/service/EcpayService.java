// EcpayService.java
package com.example.springtest.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.net.URLEncoder;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.springtest.DTO.OrderResponse;
import com.example.springtest.model.Order;
import com.example.springtest.model.OrderItem;

@Service
public class EcpayService {

    // 💡 這些值應從 application.yml 載入 (假設已在 application.yml 中配置)
    @Value("${ecpay.merchant-id}")
    private String merchantId;

    @Value("${ecpay.hash-key}")
    private String hashKey;

    @Value("${ecpay.hash-iv}")
    private String hashIV;

    // 綠界測試環境的交易網址
    private final String ecpayUrl = "https://payment-stage.ecpay.com.tw/Cashier/AioCheckOut/V5";

    // 交易完成後，綠界以 POST 方式回傳結果到您的後端 (需是外網可存取的)
    private final String returnUrl = "https://christiane-semimonarchic-dancingly.ngrok-free.dev/api/ecpay/callback";

    // 交易成功後，前端會被導向的這個頁面
    private final String clientBackUrl = "https://christiane-semimonarchic-dancingly.ngrok-free.dev/api/ecpay/order-completed";

    public OrderResponse createPaymentRequest(Order order) {

        // 1. 生成唯一的 MerchantTradeNo
        String merchantTradeNo = generateMerchantTradeNo();
        order.setTradeNo(merchantTradeNo);

        int amount = (int) Math.round(order.getTotalAmount());

        // 2. 準備參數
        Map<String, String> params = new Hashtable<>();
        params.put("MerchantID", merchantId);
        params.put("MerchantTradeNo", merchantTradeNo);
        params.put("MerchantTradeDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")));
        params.put("TotalAmount", String.valueOf(amount));
        params.put("TradeDesc", "OnlineOrder_" + order.getId());

        params.put("ItemName", buildItemName(order));

        params.put("PaymentType", "AIO");
        params.put("ChoosePayment", "ALL"); // 開啟所有付款方式
        params.put("ReturnURL", returnUrl);
        params.put("ClientBackURL", clientBackUrl);
        params.put("EncryptType", "1"); // SHA256

        // 3. 計算 CheckMacValue
        String checkMacValue = generateCheckMacValue(params);
        params.put("CheckMacValue", checkMacValue);

        System.out.println("====== ECPay Params ======");
        params.forEach((k, v) -> System.out.println(k + " = " + v));
        System.out.println("==========================");

        return new OrderResponse(
                order.getId(),
                order.getTotalAmount(),
                merchantTradeNo,
                this.ecpayUrl, // 💡 API URL
                params);
    }

    /**
     * ItemName 組裝（綠界 V5 標準格式）
     * 商品A x 1#商品B x 2
     */
    private String buildItemName(Order order) {
        StringBuilder sb = new StringBuilder();

        for (OrderItem item : order.getOrderItems()) {
            sb.append("Item")
                    .append(item.getProduct().getId()) // ⭐ 用商品 ID
                    .append(" x ")
                    .append(item.getQuantity())
                    .append("#");
        }

        // 移除最後一個 #
        sb.setLength(sb.length() - 1);

        String itemName = sb.toString();

        // 長度限制（V5 ≤ 200 bytes）
        if (itemName.length() > 200) {
            itemName = itemName.substring(0, 200);
        }

        return itemName;
    }

    /**
     * 驗證綠界回傳的 CheckMacValue
     */
    public boolean verifyCheckMacValue(Map<String, String> ecpayFeedback) {
        String receivedCheckMacValue = ecpayFeedback.get("CheckMacValue");
        if (receivedCheckMacValue == null)
            return false;

        Map<String, String> paramsToVerify = new Hashtable<>(ecpayFeedback);
        paramsToVerify.remove("CheckMacValue");

        String calculatedCheckMacValue = generateCheckMacValue(paramsToVerify);

        return receivedCheckMacValue.equalsIgnoreCase(calculatedCheckMacValue);
    }

    /**
     * 綠界 CheckMacValue 生成邏輯：
     * 1. 將參數字典加入 HashKey 和 HashIV
     * 2. 依字母順序排序
     * 3. 拼接成 Key=Value& 格式的字串
     * 4. 進行 URL Encode（綠界特有規則：空白轉 '+', 然後再將 '+' 轉 '%20'）
     * 5. 執行 SHA256 雜湊，並轉換成大寫
     */
    private String generateCheckMacValue(Map<String, String> params) {
        try {
            // 1. 參數排序
            Set<String> keys = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
            keys.addAll(params.keySet());

            // 2. 拼接字串：HashKey=xxx&Key1=Value1&Key2=Value2&HashIV=xxx
            StringBuilder sb = new StringBuilder();
            sb.append("HashKey=").append(hashKey);

            for (String key : keys) {
                sb.append("&")
                        .append(key)
                        .append("=")
                        .append(params.get(key));
            }

            sb.append("&HashIV=").append(hashIV);
            System.out.println("Step 2 原始字串: " + sb.toString());
            // 3. URL Encode（使用標準 URLEncoder）
            String encoded = URLEncoder.encode(sb.toString(), "UTF-8");
            System.out.println("Step 3 URLEncode: " + encoded);

            encoded = encoded.toLowerCase();
            System.out.println("Step 4 轉小寫: " + encoded);
            // 4. 根據綠界規則調整特殊字元（必須在 URLEncode 之後）
            encoded = encoded
                    .replace("%2d", "-")
                    .replace("%5f", "_")
                    .replace("%2e", ".")
                    .replace("%21", "!")
                    .replace("%2a", "*")
                    .replace("%28", "(")
                    .replace("%29", ")");

            System.out.println("Step 5 替換後: " + encoded);

            // 6. SHA256
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(encoded.getBytes(StandardCharsets.UTF_8));

            // 7. 轉大寫
            StringBuilder result = new StringBuilder();
            for (byte b : digest) {
                result.append(String.format("%02X", b));
            }

            return result.toString();

        } catch (Exception e) {
            throw new RuntimeException("CheckMacValue generate failed", e);
        }
    }

    /**
     * 產生唯一的 MerchantTradeNo (格式: yyyyMMddHHmmss + 5位亂碼)
     */
    private String generateMerchantTradeNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = UUID.randomUUID().toString().substring(0, 3).replaceAll("[^A-Za-z0-9]", "").toUpperCase();
        return "E" + timestamp + random; // 加上前綴以確保格式一致
    }

}