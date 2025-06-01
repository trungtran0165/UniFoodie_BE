package com.unifoodie.service;

import com.unifoodie.dto.PaymentRequest;
import com.unifoodie.dto.PaymentResponse;
import com.unifoodie.model.Order;
import com.unifoodie.model.OrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CheckoutService {

    @Autowired
    private OrderService orderService;

    @Autowired
    private PaymentService paymentService;

    /**
     * Complete checkout flow: Create Order -> Create Payment
     */
    public PaymentResponse processCheckout(CheckoutRequest request) {
        try {
            // 1. Create Order first
            Order order = orderService.createOrder(
                    request.getUserId(),
                    request.getItems(),
                    request.getDeliveryAddress(),
                    "PayOS", // paymentMethod
                    request.getSpecialInstructions());

            // 2. Create Payment linked to Order
            PaymentRequest paymentRequest = new PaymentRequest();
            paymentRequest.setOrderId(order.getId()); // Link to actual Order ID
            paymentRequest.setAmount(Math.round(order.getTotalAmount()));
            paymentRequest.setDescription("Đơn hàng UniFoodie - " + request.getItems().size() + " món");
            paymentRequest.setCustomerName(request.getCustomerName());
            paymentRequest.setCustomerEmail(request.getCustomerEmail());
            paymentRequest.setCustomerPhone(request.getCustomerPhone());
            paymentRequest.setItems(convertToPaymentItems(request.getItems()));

            PaymentResponse paymentResponse = paymentService.createPayment(paymentRequest);

            if ("success".equals(paymentResponse.getStatus())) {
                // Payment created successfully
                return paymentResponse;
            } else {
                // Payment creation failed, cancel the order
                orderService.cancelOrder(order.getId());
                return PaymentResponse.error("Failed to create payment: " + paymentResponse.getMessage());
            }

        } catch (Exception e) {
            return PaymentResponse.error("Checkout failed: " + e.getMessage());
        }
    }

    private List<PaymentRequest.PaymentItem> convertToPaymentItems(List<OrderItem> orderItems) {
        return orderItems.stream()
                .map(item -> {
                    PaymentRequest.PaymentItem paymentItem = new PaymentRequest.PaymentItem();
                    paymentItem.setName(item.getName());
                    paymentItem.setQuantity(item.getQuantity());
                    paymentItem.setPrice(Math.round(item.getPrice()));
                    return paymentItem;
                })
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * DTO for checkout request
     */
    public static class CheckoutRequest {
        private String userId;
        private List<OrderItem> items;
        private String deliveryAddress;
        private String specialInstructions;
        private String customerName;
        private String customerEmail;
        private String customerPhone;

        // Getters and Setters
        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public List<OrderItem> getItems() {
            return items;
        }

        public void setItems(List<OrderItem> items) {
            this.items = items;
        }

        public String getDeliveryAddress() {
            return deliveryAddress;
        }

        public void setDeliveryAddress(String deliveryAddress) {
            this.deliveryAddress = deliveryAddress;
        }

        public String getSpecialInstructions() {
            return specialInstructions;
        }

        public void setSpecialInstructions(String specialInstructions) {
            this.specialInstructions = specialInstructions;
        }

        public String getCustomerName() {
            return customerName;
        }

        public void setCustomerName(String customerName) {
            this.customerName = customerName;
        }

        public String getCustomerEmail() {
            return customerEmail;
        }

        public void setCustomerEmail(String customerEmail) {
            this.customerEmail = customerEmail;
        }

        public String getCustomerPhone() {
            return customerPhone;
        }

        public void setCustomerPhone(String customerPhone) {
            this.customerPhone = customerPhone;
        }
    }
}