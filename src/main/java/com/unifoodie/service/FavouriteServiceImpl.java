package com.unifoodie.service;

import com.unifoodie.model.Favourite;
import com.unifoodie.repository.FavouriteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class FavouriteServiceImpl implements FavouriteService {

    @Autowired
    private FavouriteRepository favouriteRepository;

    @Override
    public Favourite getOrCreateFavouriteList(String userId) {
        return favouriteRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Favourite newFavouriteList = new Favourite();
                    newFavouriteList.setUserId(userId);
                    return favouriteRepository.save(newFavouriteList);
                });
    }

    @Override
    public Favourite addFoodToFavouriteList(String userId, String foodId) {
        Favourite favouriteList = getOrCreateFavouriteList(userId);
        favouriteList.addFoodId(foodId);
        return favouriteRepository.save(favouriteList);
    }

    @Override
    public Favourite removeFoodFromFavouriteList(String userId, String foodId) {
        Favourite favouriteList = getOrCreateFavouriteList(userId);
        favouriteList.removeFoodId(foodId);
        return favouriteRepository.save(favouriteList);
    }

    @Override
    public boolean isFoodFavourite(String userId, String foodId) {
        Optional<Favourite> favouriteList = favouriteRepository.findByUserId(userId);
        return favouriteList.map(list -> list.getFoodIds().contains(foodId)).orElse(false);
    }
} 