package com.unifoodie.service;

import com.unifoodie.model.Food;
import com.unifoodie.repository.FoodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class FoodService {
    
    @Autowired
    private FoodRepository foodRepository;
    
    public List<String> getAllCategories() {
        return foodRepository.findDistinctCategory();
    }
    
    public List<Food> searchFoods(String keyword) {
        return foodRepository.searchByKeyword(keyword);
    }
    
    public Food getFoodById(String id) {
        return foodRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Food not found with id: " + id));
    }
    
    public List<Food> getFoodsByCategory(String category) {
        return foodRepository.findByCategory(category);
    }
} 