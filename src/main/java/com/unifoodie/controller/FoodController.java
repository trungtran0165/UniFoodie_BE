package com.unifoodie.controller;

import com.unifoodie.model.Food;
import com.unifoodie.service.FoodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.io.IOException;
import java.util.Map;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("/api/foods")
@CrossOrigin(origins = "*")
public class FoodController {

    @Autowired
    private FoodService foodService;

    @Autowired // Inject Cloudinary
    private Cloudinary cloudinary;

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getAllCategories() {
        return ResponseEntity.ok(foodService.getAllCategories());
    }

    @GetMapping("/search")
    public ResponseEntity<List<Food>> searchFoods(@RequestParam String keyword) {
        return ResponseEntity.ok(foodService.searchFoods(keyword));
    }

    @GetMapping
    public ResponseEntity<List<Food>> getAllFoods() {
        List<Food> foods = foodService.getAllFoods();
        return new ResponseEntity<>(foods, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Food> getFoodById(@PathVariable String id) {
        Optional<Food> food = foodService.getFoodById(id);
        return food.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                    .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<Food>> getFoodsByCategory(@PathVariable String category) {
        List<Food> foods = foodService.getFoodsByCategory(category);
        return new ResponseEntity<>(foods, HttpStatus.OK);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createFood(@RequestBody Food food) {
        try {
            // The image is expected to be a Base64 string in the Food object's image field
            // The service layer should handle decoding and saving the image

            Food createdFood = foodService.createFood(food);
            return ResponseEntity.ok(createdFood);
        } catch (Exception e) {
             e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create food item: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Food> updateFood(@PathVariable String id, @RequestBody Food food) {
        return ResponseEntity.ok(foodService.updateFood(id, food));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Food> patchFood(@PathVariable String id, @RequestBody Food food) {
        Food updated = foodService.patchFood(id, food);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/visibility")
    public ResponseEntity<Food> toggleAvailable(@PathVariable String id) {
        return ResponseEntity.ok(foodService.toggleAvailable(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFood(@PathVariable String id) {
        foodService.deleteFood(id);
        return ResponseEntity.ok().build();
    }
} 