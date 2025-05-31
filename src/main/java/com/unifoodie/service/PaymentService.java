package com.unifoodie.service;

import com.unifoodie.dto.PaymentRequest;
import com.unifoodie.dto.PaymentResponse;
import com.unifoodie.dto.PayOSRequest;
import com.unifoodie.model.Payment;
import com.unifoodie.repository.PaymentRepository;
import com.unifoodie.config.PayOSConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PayOSConfig payOSConfig;

    @Value("${payos.return-url}")
    private String returnUrl;

    @Value("${payos.cancel-url}")
    private String cancelUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public PaymentResponse createPayment(PaymentRequest request) {
        try {
            // Generate unique order code (6-digit as required by PayOS)
            String orderCode = generateOrderCode();
            long orderCodeLong = Long.parseLong(orderCode);

            // Create payment record
            Payment payment = new Payment(orderCode, request.getOrderId(), request.getAmount(),
                    request.getDescription());
            payment.setCustomerName(request.getCustomerName());
            payment.setCustomerEmail(request.getCustomerEmail());
            payment.setCustomerPhone(request.getCustomerPhone());

            // Convert items to PayOS format
            List<PayOSRequest.PayOSItem> payOSItems = convertToPayOSItems(request.getItems(), request.getAmount());

            // Create PayOS request with proper structure
            PayOSRequest payOSRequest = new PayOSRequest(
                    orderCodeLong,
                    request.getAmount(),
                    request.getDescription(),
                    returnUrl + "?orderCode=" + orderCode,
                    cancelUrl + "?orderCode=" + orderCode,
                    payOSItems);

            // Set buyer information
            payOSRequest.setBuyerName(
                    request.getCustomerName() != null ? request.getCustomerName() : "Khách hàng UniFoodie");
            payOSRequest.setBuyerEmail(
                    request.getCustomerEmail() != null ? request.getCustomerEmail() : "customer@unifoodie.com");
            payOSRequest.setBuyerPhone(request.getCustomerPhone() != null ? request.getCustomerPhone() : "0123456789");
            payOSRequest.setBuyerAddress("UniFoodie Vietnam");

            // Set expiration time (24 hours from now)
            payOSRequest.setExpiredAt(System.currentTimeMillis() / 1000 + 86400);

            // Create signature according to PayOS documentation
            String signature = createPayOSSignature(payOSRequest);
            payOSRequest.setSignature(signature);

            // Call PayOS API
            String paymentUrl = callPayOSCreatePayment(payOSRequest);

            if (paymentUrl != null && !paymentUrl.isEmpty()) {
                payment.setPaymentUrl(paymentUrl);
                paymentRepository.save(payment);
                return PaymentResponse.success(orderCode, paymentUrl, "payos-checkout");
            } else {
                // Fallback to demo payment page
                String demoPaymentUrl = "http://localhost:5173/demo-payment?orderCode=" + orderCode + "&amount="
                        + request.getAmount();
                payment.setPaymentUrl(demoPaymentUrl);
                paymentRepository.save(payment);
                return PaymentResponse.success(orderCode, demoPaymentUrl, "demo-payment");
            }

        } catch (Exception e) {
            System.err.println("Payment creation error: " + e.getMessage());
            e.printStackTrace();
            return PaymentResponse.error("Failed to create payment: " + e.getMessage());
        }
    }

    private String callPayOSCreatePayment(PayOSRequest payOSRequest) {
        try {
            String url = "https://api-merchant.payos.vn/v2/payment-requests";

            System.out.println("=== PayOS API Call Debug ===");
            System.out.println("URL: " + url);
            System.out.println("Client ID: " + payOSConfig.getClientId());
            System.out.println("Order Code: " + payOSRequest.getOrderCode());
            System.out.println("Amount: " + payOSRequest.getAmount());
            System.out.println("Description: " + payOSRequest.getDescription());
            System.out.println("Return URL: " + payOSRequest.getReturnUrl());
            System.out.println("Cancel URL: " + payOSRequest.getCancelUrl());
            System.out.println("Signature: " + payOSRequest.getSignature());

            // Create headers according to PayOS documentation
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-client-id", payOSConfig.getClientId());
            headers.set("x-api-key", payOSConfig.getApiKey());
            headers.set("Content-Type", "application/json");

            HttpEntity<PayOSRequest> entity = new HttpEntity<>(payOSRequest, headers);

            // Make the API call
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

            System.out.println("PayOS Response Status: " + response.getStatusCode());
            System.out.println("PayOS Response Body: " + response.getBody());

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                String code = (String) responseBody.get("code");

                if ("00".equals(code)) {
                    Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                    if (data != null) {
                        String checkoutUrl = (String) data.get("checkoutUrl");
                        System.out.println("PayOS Checkout URL: " + checkoutUrl);
                        return checkoutUrl;
                    }
                } else {
                    String desc = (String) responseBody.get("desc");
                    System.err.println("PayOS API error - Code: " + code + ", Description: " + desc);
                }
            }
        } catch (Exception e) {
            System.err.println("PayOS API call failed: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private String createPayOSSignature(PayOSRequest payOSRequest) {
        try {
            // According to PayOS docs:
            // amount=$amount&cancelUrl=$cancelUrl&description=$description&orderCode=$orderCode&returnUrl=$returnUrl
            String data = String.format("amount=%d&cancelUrl=%s&description=%s&orderCode=%d&returnUrl=%s",
                    payOSRequest.getAmount(),
                    payOSRequest.getCancelUrl(),
                    payOSRequest.getDescription(),
                    payOSRequest.getOrderCode(),
                    payOSRequest.getReturnUrl());

            System.out.println("Signature data: " + data);
            System.out.println("Checksum key: " + (payOSConfig.getChecksumKey() != null ? "***SET***" : "NULL"));

            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    payOSConfig.getChecksumKey().getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256");
            mac.init(secretKeySpec);

            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            String signature = hexString.toString();
            System.out.println("Generated signature: " + signature);
            return signature;
        } catch (Exception e) {
            System.err.println("Signature creation failed: " + e.getMessage());
            e.printStackTrace();
            return "";
        }
    }

    public void handlePaymentSuccess(String orderCode, String transactionId) {
        Optional<Payment> paymentOpt = paymentRepository.findByOrderCode(orderCode);
        if (paymentOpt.isPresent()) {
            Payment payment = paymentOpt.get();
            payment.setStatus("SUCCESS");
            payment.setTransactionId(transactionId);
            paymentRepository.save(payment);

            // TODO: Update order status in your order management system
            // orderService.updateOrderStatus(payment.getOrderId(), "PAID");
        }
    }

    public void handlePaymentCancel(String orderCode) {
        Optional<Payment> paymentOpt = paymentRepository.findByOrderCode(orderCode);
        if (paymentOpt.isPresent()) {
            Payment payment = paymentOpt.get();
            payment.setStatus("CANCELLED");
            paymentRepository.save(payment);
        }
    }

    public void handlePaymentFailed(String orderCode, String reason) {
        Optional<Payment> paymentOpt = paymentRepository.findByOrderCode(orderCode);
        if (paymentOpt.isPresent()) {
            Payment payment = paymentOpt.get();
            payment.setStatus("FAILED");
            paymentRepository.save(payment);
        }
    }

    public Optional<Payment> getPaymentByOrderCode(String orderCode) {
        return paymentRepository.findByOrderCode(orderCode);
    }

    public Optional<Payment> getPaymentByOrderId(String orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    public List<Payment> getPaymentsByStatus(String status) {
        return paymentRepository.findByStatus(status);
    }

    private String generateOrderCode() {
        // Generate 6-digit order code (PayOS requirement)
        // Using current timestamp modulo to ensure 6 digits
        long timestamp = System.currentTimeMillis();
        long orderCode = 100000 + (timestamp % 900000); // Ensures 6 digits (100000-999999)
        return String.valueOf(orderCode);
    }

    private List<PayOSRequest.PayOSItem> convertToPayOSItems(List<PaymentRequest.PaymentItem> items, Long totalAmount) {
        if (items == null || items.isEmpty()) {
            // Default item if no items provided
            return List.of(new PayOSRequest.PayOSItem("Đơn hàng UniFoodie", 1, totalAmount));
        }

        return items.stream()
                .map(item -> new PayOSRequest.PayOSItem(
                        item.getName(),
                        item.getQuantity(),
                        item.getPrice()))
                .collect(Collectors.toList());
    }
}