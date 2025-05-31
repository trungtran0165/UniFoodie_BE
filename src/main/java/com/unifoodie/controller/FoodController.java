package com.unifoodie.controller;

import com.unifoodie.model.Food;
import com.unifoodie.service.FoodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class FoodController {

    @Autowired
    private FoodService foodService;

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getAllCategories() {
        return ResponseEntity.ok(foodService.getAllCategories());
    }

    @GetMapping("/foods/search")
    public ResponseEntity<List<Food>> searchFoods(@RequestParam String keyword) {
        return ResponseEntity.ok(foodService.searchFoods(keyword));
    }

    @GetMapping("/foods/{id}")
    public ResponseEntity<Food> getFoodById(@PathVariable String id) {
        return ResponseEntity.ok(foodService.getFoodById(id));
    }

    @GetMapping("/foods/category/{category}")
    public ResponseEntity<List<Food>> getFoodsByCategory(@PathVariable String category) {
        return ResponseEntity.ok(foodService.getFoodsByCategory(category));
    }
} 