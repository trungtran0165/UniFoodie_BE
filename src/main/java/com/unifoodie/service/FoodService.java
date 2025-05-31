package com.unifoodie.service;

import com.unifoodie.model.Food;
import com.unifoodie.repository.FoodRepository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

public interface FoodService {
    List<Food> getAllFoods();
    Optional<Food> getFoodById(String id);
    List<Food> getFoodsByCategory(String category);
    Food createFood(Food food);
    Food updateFood(String id, Food foodDetails);
    void deleteFood(String id);
    Food toggleAvailable(String id);
    Food patchFood(String id, Food foodDetails);
    List<String> getAllCategories();
    List<Food> searchFoods(String keyword);
    // Add other methods for creating, updating, deleting food items if needed
} 