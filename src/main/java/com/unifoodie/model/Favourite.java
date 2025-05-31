package com.unifoodie.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;
import java.util.ArrayList;

// Assuming Lombok is configured, otherwise add getters/setters/constructors manually
// import lombok.Data;
// import lombok.NoArgsConstructor;
// import lombok.AllArgsConstructor;

@Document(collection = "favourites")
// @Data
// @NoArgsConstructor
// @AllArgsConstructor
public class Favourite {

    @Id
    private String id;

    private String userId; // Link favourite list to a user

    private List<String> foodIds = new ArrayList<>(); // List of food item IDs the user has favourited

    // Manual Constructors (if not using Lombok)
    public Favourite() {
    }

    public Favourite(String id, String userId, List<String> foodIds) {
        this.id = id;
        this.userId = userId;
        this.foodIds = foodIds;
         if (this.foodIds == null) {
            this.foodIds = new ArrayList<>();
        }
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

    public List<String> getFoodIds() {
        return foodIds;
    }

    public void setFoodIds(List<String> foodIds) {
        this.foodIds = foodIds;
    }

     public void addFoodId(String foodId) {
        if (this.foodIds == null) {
            this.foodIds = new ArrayList<>();
        }
        if (!this.foodIds.contains(foodId)) {
            this.foodIds.add(foodId);
        }
    }

    public void removeFoodId(String foodId) {
        if (this.foodIds != null) {
            this.foodIds.remove(foodId);
        }
    }
} 