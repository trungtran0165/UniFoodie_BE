package com.unifoodie.repository;

import com.unifoodie.model.Category;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface CategoryRepository extends MongoRepository<Category, String> {
    List<Category> findByActive(boolean active);
} 