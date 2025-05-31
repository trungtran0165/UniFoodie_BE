package com.unifoodie.controller;

import com.unifoodie.model.Cart;
import com.unifoodie.model.CartItem;
import com.unifoodie.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carts")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping("/{userId}")
    public ResponseEntity<Cart> getCartByUserId(@PathVariable String userId) {
        Cart cart = cartService.getOrCreateCart(userId);
        return new ResponseEntity<>(cart, HttpStatus.OK);
    }

    @PostMapping("/{userId}/items")
    public ResponseEntity<Cart> addItemToCart(@PathVariable String userId, @RequestBody CartItem item) {
        Cart updatedCart = cartService.addItemToCart(userId, item);
        return new ResponseEntity<>(updatedCart, HttpStatus.OK);
    }

    @PutMapping("/{userId}/items/{foodId}")
    public ResponseEntity<Cart> updateItemQuantity(
            @PathVariable String userId,
            @PathVariable String foodId,
            @RequestParam int quantity) {
        Cart updatedCart = cartService.updateItemQuantity(userId, foodId, quantity);
        return new ResponseEntity<>(updatedCart, HttpStatus.OK);
    }

    @DeleteMapping("/{userId}/items/{foodId}")
    public ResponseEntity<Cart> removeItemFromCart(@PathVariable String userId, @PathVariable String foodId) {
        Cart updatedCart = cartService.removeItemFromCart(userId, foodId);
        return new ResponseEntity<>(updatedCart, HttpStatus.OK);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> clearCart(@PathVariable String userId) {
        cartService.clearCart(userId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
} 