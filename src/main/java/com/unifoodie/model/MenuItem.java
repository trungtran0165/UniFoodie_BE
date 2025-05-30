package com.unifoodie.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;

@Data
@Document(collection = "menu_items")
public class MenuItem {
    @Id
    private String id;

    @Field("name")
    private String name;

    @Field("description")
    private String description;

    @Field("price")
    private BigDecimal price;

    @Field("category")
    private String category;

    @Field("image_url")
    private String imageUrl;

    @Field("available")
    private boolean available = true;
}