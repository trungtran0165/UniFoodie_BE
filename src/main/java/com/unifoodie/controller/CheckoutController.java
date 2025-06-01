package com.unifoodie.controller;

import com.unifoodie.dto.PaymentResponse;
import com.unifoodie.service.CheckoutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/checkout")
@CrossOrigin(origins = "*")
public class CheckoutController {

    @Autowired
    private CheckoutService checkoutService;

    /**
     * Complete checkout: Create Order + Payment
     */
    @PostMapping("/process")
    public ResponseEntity<PaymentResponse> processCheckout(@RequestBody CheckoutService.CheckoutRequest request) {
        try {
            // Validate request
            if (request.getUserId() == null || request.getItems() == null || request.getItems().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(PaymentResponse.error("Invalid request: userId and items are required"));
            }

            if (request.getDeliveryAddress() == null || request.getDeliveryAddress().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(PaymentResponse.error("Delivery address is required"));
            }

            // Process checkout
            PaymentResponse response = checkoutService.processCheckout(request);

            if ("success".equals(response.getStatus())) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(PaymentResponse.error("Checkout failed: " + e.getMessage()));
        }
    }
}