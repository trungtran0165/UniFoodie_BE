package com.unifoodie.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;
import java.util.ArrayList;
//import java.util.Date; // Remove this import

// Assuming Lombok is configured, otherwise add getters/setters/constructors manually
// import lombok.Data;
// import lombok.NoArgsConstructor;
// import lombok.AllArgsConstructor;

@Document(collection = "orders")
// @Data
// @NoArgsConstructor
// @AllArgsConstructor
public class Order {

    @Id
    private String id;

    private String userId; // Link order to a user

    private List<OrderItem> items = new ArrayList<>();

    private double totalAmount;

    private String deliveryAddress;

    private String paymentMethod; // e.g., "CASH", "CARD", "MOMO"

    private String status; // e.g., "PENDING", "CONFIRMED", "PREPARING", "READY", "DELIVERED", "CANCELLED"

    private String paymentStatus; // e.g., "PENDING", "COMPLETED", "FAILED" - Added this field

    private String specialInstructions; // Optional special instructions from the user

    private String createdAt; // Changed from Date to String
    private String updatedAt; // Changed from Date to String

    // Manual Constructors (if not using Lombok)
    public Order() {
    }

    // Update constructor to accept String for dates and include paymentStatus
    public Order(String id, String userId, List<OrderItem> items, double totalAmount, String deliveryAddress, String paymentMethod, String status, String paymentStatus, String specialInstructions, String createdAt, String updatedAt) {
        this.id = id;
        this.userId = userId;
        this.items = items;
         if (this.items == null) {
            this.items = new ArrayList<>();
        }
        this.totalAmount = totalAmount;
        this.deliveryAddress = deliveryAddress;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.paymentStatus = paymentStatus;
        this.specialInstructions = specialInstructions;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Manual Getters and Setters (if not using Lombok)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Add getter and setter for paymentStatus
    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getSpecialInstructions() {
        return specialInstructions;
    }

    public void setSpecialInstructions(String specialInstructions) {
        this.specialInstructions = specialInstructions;
    }

    // Add getters and setters for createdAt and updatedAt (String type)
    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

     public void addOrderItem(OrderItem item) {
        if (this.items == null) {
            this.items = new ArrayList<>();
        }
        this.items.add(item);
    }
} 