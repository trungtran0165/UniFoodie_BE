package com.unifoodie.controller;

import com.unifoodie.model.Favourite;
import com.unifoodie.service.FavouriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/favourites")
public class FavouriteController {

    @Autowired
    private FavouriteService favouriteService;

    @GetMapping("/{userId}")
    public ResponseEntity<Favourite> getFavouriteListByUserId(@PathVariable String userId) {
        Favourite favouriteList = favouriteService.getOrCreateFavouriteList(userId);
        return new ResponseEntity<>(favouriteList, HttpStatus.OK);
    }

    @PostMapping("/{userId}/add/{foodId}")
    public ResponseEntity<Favourite> addFoodToFavouriteList(
            @PathVariable String userId,
            @PathVariable String foodId) {
        Favourite updatedFavouriteList = favouriteService.addFoodToFavouriteList(userId, foodId);
        return new ResponseEntity<>(updatedFavouriteList, HttpStatus.OK);
    }

    @DeleteMapping("/{userId}/remove/{foodId}")
    public ResponseEntity<Favourite> removeFoodFromFavouriteList(
            @PathVariable String userId,
            @PathVariable String foodId) {
        Favourite updatedFavouriteList = favouriteService.removeFoodFromFavouriteList(userId, foodId);
        return new ResponseEntity<>(updatedFavouriteList, HttpStatus.OK);
    }

    @GetMapping("/{userId}/is-favourite/{foodId}")
    public ResponseEntity<Boolean> isFoodFavourite(
            @PathVariable String userId,
            @PathVariable String foodId) {
        boolean isFavourite = favouriteService.isFoodFavourite(userId, foodId);
        return new ResponseEntity<>(isFavourite, HttpStatus.OK);
    }
} 