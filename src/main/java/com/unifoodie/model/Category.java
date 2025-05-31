package com.unifoodie.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

// Assuming Lombok is configured, otherwise add getters/setters/constructors manually
// import lombok.Data;
// import lombok.NoArgsConstructor;
// import lombok.AllArgsConstructor;

@Document(collection = "categories")
// @Data
// @NoArgsConstructor
// @AllArgsConstructor
public class Category {

    @Id
    private String id;

    private String name;

    private String description;

    private String imageUrl;

    private boolean active = true; // Default to active

    // Manual Constructors (if not using Lombok)
    public Category() {
    }

    public Category(String id, String name, String description, String imageUrl, boolean active) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.active = active;
    }

    // Manual Getters and Setters (if not using Lombok)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "Category{" +
               "id='" + id + '\'' +
               ", name='" + name + '\'' +
               ", description='" + description + '\'' +
               ", imageUrl='" + imageUrl + '\'' +
               ", active=" + active +
               '}';
    }
} 