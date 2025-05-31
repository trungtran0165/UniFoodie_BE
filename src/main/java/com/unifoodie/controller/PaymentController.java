package com.unifoodie.controller;

import com.unifoodie.dto.PaymentRequest;
import com.unifoodie.dto.PaymentResponse;
import com.unifoodie.model.Payment;
import com.unifoodie.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    // Create payment link
    @PostMapping("/create")
    public ResponseEntity<PaymentResponse> createPayment(@RequestBody PaymentRequest request) {
        try {
            // Basic validation
            if (request.getOrderId() == null || request.getAmount() == null || request.getAmount() <= 0) {
                return ResponseEntity.badRequest()
                        .body(PaymentResponse.error("Invalid request data"));
            }

            PaymentResponse response = paymentService.createPayment(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(PaymentResponse.error("Internal server error: " + e.getMessage()));
        }
    }

    // Get payment status
    @GetMapping("/status/{orderCode}")
    public ResponseEntity<?> getPaymentStatus(@PathVariable String orderCode) {
        Optional<Payment> payment = paymentService.getPaymentByOrderCode(orderCode);
        if (payment.isPresent()) {
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "data", payment.get()));
        }
        return ResponseEntity.notFound().build();
    }

    // Get payment by order ID
    @GetMapping("/order/{orderId}")
    public ResponseEntity<?> getPaymentByOrderId(@PathVariable String orderId) {
        Optional<Payment> payment = paymentService.getPaymentByOrderId(orderId);
        if (payment.isPresent()) {
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "data", payment.get()));
        }
        return ResponseEntity.notFound().build();
    }

    // Get payments by status
    @GetMapping("/status")
    public ResponseEntity<?> getPaymentsByStatus(@RequestParam(defaultValue = "PENDING") String status) {
        List<Payment> payments = paymentService.getPaymentsByStatus(status);
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "data", payments));
    }

    // PayOS Return URL Handler
    @GetMapping("/return")
    public RedirectView handlePaymentReturn(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String id,
            @RequestParam(required = false) String cancel,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String orderCode,
            @RequestParam(required = false) String demo) {

        try {
            System.out.println("=== Payment Return Handler ===");
            System.out.println("OrderCode: " + orderCode);
            System.out.println("Status: " + status);
            System.out.println("Cancel: " + cancel);
            System.out.println("Demo: " + demo);
            System.out.println("Code: " + code);
            System.out.println("ID: " + id);

            if ("true".equals(demo)) {
                // Demo payment - auto success
                if (orderCode != null) {
                    paymentService.handlePaymentSuccess(orderCode, "demo-transaction-" + System.currentTimeMillis());
                }
                return new RedirectView("http://localhost:5173/payment-success?orderCode=" + orderCode);
            } else if ("true".equals(cancel)) {
                // Payment cancelled
                if (orderCode != null) {
                    paymentService.handlePaymentCancel(orderCode);
                }
                return new RedirectView(
                        "http://localhost:5173/payment-success?orderCode=" + orderCode + "&status=cancelled");
            } else if ("success".equals(status) || "PAID".equals(status)) {
                // Payment successful
                if (orderCode != null) {
                    paymentService.handlePaymentSuccess(orderCode,
                            id != null ? id : "success-" + System.currentTimeMillis());
                }
                return new RedirectView("http://localhost:5173/payment-success?orderCode=" + orderCode);
            } else {
                // Payment failed or other status
                if (orderCode != null) {
                    paymentService.handlePaymentFailed(orderCode, status);
                }
                return new RedirectView(
                        "http://localhost:5173/payment-success?orderCode=" + orderCode + "&status=failed");
            }
        } catch (Exception e) {
            System.err.println("Payment return handler error: " + e.getMessage());
            e.printStackTrace();
            return new RedirectView("http://localhost:5173/payment-success?error=true");
        }
    }

    // PayOS Cancel URL Handler
    @GetMapping("/cancel")
    public RedirectView handlePaymentCancel(@RequestParam String orderCode) {
        try {
            System.out.println("=== Payment Cancel Handler ===");
            System.out.println("OrderCode: " + orderCode);

            paymentService.handlePaymentCancel(orderCode);
            return new RedirectView(
                    "http://localhost:5173/payment-success?orderCode=" + orderCode + "&status=cancelled");
        } catch (Exception e) {
            System.err.println("Payment cancel handler error: " + e.getMessage());
            return new RedirectView("http://localhost:5173/payment-success?error=true");
        }
    }

    // PayOS Webhook Handler
    @PostMapping("/webhook")
    public ResponseEntity<?> handleWebhook(@RequestBody Map<String, Object> payload) {
        try {
            // Extract webhook data
            String orderCode = String.valueOf(payload.get("orderCode"));
            String status = (String) payload.get("code");
            String transactionId = (String) payload.get("id");

            // Handle based on status
            if ("00".equals(status)) {
                // Success
                paymentService.handlePaymentSuccess(orderCode, transactionId);
            } else {
                // Failed
                paymentService.handlePaymentFailed(orderCode, "Payment failed with code: " + status);
            }

            return ResponseEntity.ok(Map.of("status", "success"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("status", "error", "message", e.getMessage()));
        }
    }
}