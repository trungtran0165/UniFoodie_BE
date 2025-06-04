package com.unifoodie.repository;

import com.unifoodie.model.Food;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.util.List;
import java.util.Optional;

public interface FoodRepository extends MongoRepository<Food, String> {
    List<Food> findByCategory(String category);

    // Find by numeric id field
    Optional<Food> findById(int id);

    // Find by ObjectId (_id) - this is inherited from MongoRepository<Food, String>
    // Optional<Food> findById(String objectId); // Already available from
    // MongoRepository

    // Explicit method to find by ObjectId for clarity
    @Query("{'_id': ?0}")
    Optional<Food> findByObjectId(String objectId);

    @Query("{'$or': [{'name': {$regex: ?0, $options: 'i'}}, " +
            "{'description': {$regex: ?0, $options: 'i'}}, " +
            "{'ingredients': {$regex: ?0, $options: 'i'}}]}")
    List<Food> searchByKeyword(String keyword);
}