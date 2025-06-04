package com.unifoodie.model;

import org.springframework.data.mongodb.core.mapping.Field;

// Assuming Lombok is configured, otherwise add getters/setters/constructors manually
// import lombok.Data;
// import lombok.NoArgsConstructor;
// import lombok.AllArgsConstructor;

// CartItem will be an embedded document, so no @Document annotation here
// @Data
// @NoArgsConstructor
// @AllArgsConstructor
public class CartItem {

    private String foodId;
    private String name;
    private double price;
    private int quantity;
    private String imageUrl; // Optional: store image URL for display in cart (legacy)
    private String image; // New field to match Food model

    // Manual Constructors (if not using Lombok)
    public CartItem() {
    }

    public CartItem(String foodId, String name, double price, int quantity, String imageUrl) {
        this.foodId = foodId;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
        this.image = imageUrl; // Set both for compatibility
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
        this.image = imageUrl; // Keep both in sync
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
        this.imageUrl = image; // Keep both in sync
    }
}