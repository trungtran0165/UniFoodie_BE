package com.unifoodie.repository;

import com.unifoodie.model.Favourite;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.List;

public interface FavouriteRepository extends MongoRepository<Favourite, String> {
    Optional<Favourite> findByUserId(String userId);
    List<Favourite> findByFoodIdsContaining(String foodId);
} 