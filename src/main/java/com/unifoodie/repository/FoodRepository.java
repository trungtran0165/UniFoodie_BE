package com.unifoodie.repository;

import com.unifoodie.model.Food;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.util.List;
import java.util.Optional;

public interface FoodRepository extends MongoRepository<Food, String> {
    List<Food> findByCategory(String category);
    
    Optional<Food> findById(int id);
    
    @Query("{'$or': [{'name': {$regex: ?0, $options: 'i'}}, " +
           "{'description': {$regex: ?0, $options: 'i'}}, " +
           "{'ingredients': {$regex: ?0, $options: 'i'}}]}")
    List<Food> searchByKeyword(String keyword);
} 