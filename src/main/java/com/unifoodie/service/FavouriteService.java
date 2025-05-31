package com.unifoodie.service;

import com.unifoodie.model.Favourite;

public interface FavouriteService {
    Favourite getOrCreateFavouriteList(String userId);
    Favourite addFoodToFavouriteList(String userId, String foodId);
    Favourite removeFoodFromFavouriteList(String userId, String foodId);
    boolean isFoodFavourite(String userId, String foodId);
} 