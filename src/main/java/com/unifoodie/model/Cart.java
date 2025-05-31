package com.unifoodie.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;
import java.util.ArrayList;

// Assuming Lombok is configured, otherwise add getters/setters/constructors manually
// import lombok.Data;
// import lombok.NoArgsConstructor;
// import lombok.AllArgsConstructor;

@Document(collection = "carts")
// @Data
// @NoArgsConstructor
// @AllArgsConstructor
public class Cart {

    @Id
    private String id;

    private String userId; // Link cart to a user

    private List<CartItem> items = new ArrayList<>();

    private double totalAmount;

    private String createdAt; // Changed from Date to String
    private String updatedAt; // Changed from Date to String

    // Manual Constructors (if not using Lombok)
    public Cart() {
    }

    public Cart(String id, String userId, List<CartItem> items, double totalAmount, String createdAt, String updatedAt) {
        this.id = id;
        this.userId = userId;
        this.items = items; // Use provided list or new ArrayList if null
        if (this.items == null) {
            this.items = new ArrayList<>();
        }
        this.totalAmount = totalAmount;
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

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
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

     public void addItem(CartItem item) {
        if (this.items == null) {
            this.items = new ArrayList<>();
        } else if (this.items == null) {
            this.items = new ArrayList<>();
        }
        this.items.add(item);
    }

    public void removeItem(CartItem item) {
        if (this.items != null) {
            this.items.remove(item);
        }
    }

    // Method to calculate total amount - useful to have here or in service
    public void calculateTotalAmount() {
        this.totalAmount = items.stream()
                                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                                .sum();
    }
} 