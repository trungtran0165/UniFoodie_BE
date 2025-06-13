package com.unifoodie.service;

import com.unifoodie.model.Food;
import com.unifoodie.repository.FoodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import org.bson.types.ObjectId;

@Service
public class FoodServiceImpl implements FoodService {

    @Autowired
    private FoodRepository foodRepository;
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public List<String> getAllCategories() {
        return mongoTemplate.findDistinct(
                new org.springframework.data.mongodb.core.query.Query(),
                "category",
                "foods",
                String.class);
    }

    @Override
    public List<Food> searchFoods(String keyword) {
        return foodRepository.searchByKeyword(keyword);
    }

    @Override
    public List<Food> getAllFoods() {
        return foodRepository.findAll();
    }

    @Override
    public Optional<Food> getFoodById(String id) {
        try {
            // Try to find by MongoDB ObjectId (_id) first
            Optional<Food> foodByObjectId = foodRepository.findById(id);
            if (foodByObjectId.isPresent()) {
                System.out.println("Found food by ObjectId: " + id);
                return foodByObjectId;
            }

            // Fallback: try to parse as integer and find by numeric id field
            try {
                int foodId = Integer.parseInt(id);
                Optional<Food> foodByNumericId = foodRepository.findById(foodId);
                if (foodByNumericId.isPresent()) {
                    System.out.println("Found food by numeric id: " + foodId);
                    return foodByNumericId;
                }
            } catch (NumberFormatException e2) {
                // Not a valid integer, continue
            }

            System.out.println("Food not found with id: " + id);
            return Optional.empty();

        } catch (Exception e) {
            System.err.println("Error finding food with id: " + id + ", error: " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public List<Food> getFoodsByCategory(String category) {
        return foodRepository.findByCategory(category);
    }

    @Override
    @Transactional
    public Food createFood(Food food) {
        // MongoDB will auto-generate _id if not set
        return foodRepository.save(food);
    }

    @Override
    @Transactional
    public Food updateFood(String id, Food foodDetails) {
        Food food = getFoodById(id)
                .orElseThrow(() -> new RuntimeException("Food not found with id: " + id));
        // Update fields - _id is not updated
        food.setName(foodDetails.getName());
        food.setDescription(foodDetails.getDescription());
        food.setImage(foodDetails.getImage());
        food.setPrice(foodDetails.getPrice());
        food.setIngredients(foodDetails.getIngredients());
        food.setCategory(foodDetails.getCategory());
        return foodRepository.save(food);
    }

    @Override
    @Transactional
    public Food toggleAvailable(String id) {
        Food food = getFoodById(id)
                .orElseThrow(() -> new RuntimeException("Food not found with id: " + id));
        food.setAvailable(!food.isAvailable());
        return foodRepository.save(food);
    }

    @Override
    @Transactional
    public void deleteFood(String id) {
        foodRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Food patchFood(String id, Food foodDetails) {
        Food food = getFoodById(id)
                .orElseThrow(() -> new RuntimeException("Food not found with id: " + id));
        if (foodDetails.getName() != null) food.setName(foodDetails.getName());
        if (foodDetails.getDescription() != null) food.setDescription(foodDetails.getDescription());
        if (foodDetails.getImage() != null) food.setImage(foodDetails.getImage());
        if (foodDetails.getPrice() != 0) food.setPrice(foodDetails.getPrice());
        if (foodDetails.getIngredients() != null) food.setIngredients(foodDetails.getIngredients());
        if (foodDetails.getCategory() != null) food.setCategory(foodDetails.getCategory());
        // Nếu muốn cập nhật available:
        food.setAvailable(foodDetails.isAvailable());
        return foodRepository.save(food);
    }
}