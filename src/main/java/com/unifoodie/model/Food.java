package com.unifoodie.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;

@Data
@Document(collection = "foods")
public class Food {
    @Id
    private String id;
    private String name;
    private String description;
    private String image;
    private Double price;
    private String[] ingredients;
    private String category;
} 