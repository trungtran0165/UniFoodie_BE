package com.unifoodie.dto;

import com.unifoodie.model.OrderItem;
import java.util.List;

public class CreateOrderRequest {
    private String userId;
    private List<OrderItem> items;
    private String deliveryAddress;
    private String paymentMethod;
    private String specialInstructions;

    // Constructors
    public CreateOrderRequest() {
    }

    public CreateOrderRequest(String userId, List<OrderItem> items, String deliveryAddress,
            String paymentMethod, String specialInstructions) {
        this.userId = userId;
        this.items = items;
        this.deliveryAddress = deliveryAddress;
        this.paymentMethod = paymentMethod;
        this.specialInstructions = specialInstructions;
    }

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

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getSpecialInstructions() {
        return specialInstructions;
    }

    public void setSpecialInstructions(String specialInstructions) {
        this.specialInstructions = specialInstructions;
    }

    // Validation method
    public boolean isValid() {
        return userId != null && !userId.trim().isEmpty()
                && items != null && !items.isEmpty()
                && deliveryAddress != null && !deliveryAddress.trim().isEmpty()
                && paymentMethod != null && !paymentMethod.trim().isEmpty();
    }

    @Override
    public String toString() {
        return "CreateOrderRequest{" +
                "userId='" + userId + '\'' +
                ", items=" + (items != null ? items.size() + " items" : "null") +
                ", deliveryAddress='" + deliveryAddress + '\'' +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", specialInstructions='" + specialInstructions + '\'' +
                '}';
    }
}