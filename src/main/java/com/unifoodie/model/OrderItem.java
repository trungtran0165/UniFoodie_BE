package com.unifoodie.model;

// Assuming Lombok is configured, otherwise add getters/setters/constructors manually
// import lombok.Data;
// import lombok.NoArgsConstructor;
// import lombok.AllArgsConstructor;

// OrderItem will be an embedded document
// @Data
// @NoArgsConstructor
// @AllArgsConstructor
public class OrderItem {

    private String foodId;
    private String name;
    private double price;
    private int quantity;
    private String imageUrl; // Optional: store image URL at time of order

    // Manual Constructors (if not using Lombok)
    public OrderItem() {
    }

    public OrderItem(String foodId, String name, double price, int quantity, String imageUrl) {
        this.foodId = foodId;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
    }

    // Manual Getters and Setters (if not using Lombok)
    public String getFoodId() {
        return foodId;
    }

    public void setFoodId(String foodId) {
        this.foodId = foodId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
} 