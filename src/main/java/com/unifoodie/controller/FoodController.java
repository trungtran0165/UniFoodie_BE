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

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createFood(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("file") MultipartFile file,
            @RequestParam("price") double price,
            @RequestParam("category") String category)
             {
        try {
            // Upload image to Cloudinary
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            String imageUrl = (String) uploadResult.get("secure_url");

            // Create Food object
            Food food = new Food();
            food.setName(name);
            food.setDescription(description);
            food.setImage(imageUrl); // Store Cloudinary URL
            food.setPrice((int) price);
            food.setCategory(category);
            food.setIngredients(null); // Assuming ingredients are not part of this form
            food.setAvailable(true); // Assuming new food is available

            Food createdFood = foodService.createFood(food);
            return ResponseEntity.ok(createdFood);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload image or create food item.");
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