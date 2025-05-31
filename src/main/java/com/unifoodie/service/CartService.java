package com.unifoodie.service;

import com.unifoodie.model.Cart;
import com.unifoodie.model.CartItem;
import com.unifoodie.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
public class CartService {
    @Autowired
    private CartRepository cartRepository;

    private String getCurrentDateTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
    }

    public Cart getOrCreateCart(String userId) {
        return cartRepository.findByUserId(userId)
            .orElseGet(() -> {
                Cart newCart = new Cart();
                newCart.setUserId(userId);
                newCart.setCreatedAt(getCurrentDateTime());
                newCart.setUpdatedAt(getCurrentDateTime());
                return cartRepository.save(newCart);
            });
    }

    public Cart addItemToCart(String userId, CartItem item) {
        Cart cart = getOrCreateCart(userId);
        
        // Check if item already exists in cart
        Optional<CartItem> existingItem = cart.getItems().stream()
            .filter(i -> i.getFoodId().equals(item.getFoodId()))
            .findFirst();

        if (existingItem.isPresent()) {
            // Update quantity if item exists
            existingItem.get().setQuantity(existingItem.get().getQuantity() + item.getQuantity());
        } else {
            // Add new item if it doesn't exist
            cart.getItems().add(item);
        }

        // Update total amount
        updateCartTotal(cart);
        cart.setUpdatedAt(getCurrentDateTime());
        
        return cartRepository.save(cart);
    }

    public Cart updateItemQuantity(String userId, String foodId, int quantity) {
        Cart cart = getOrCreateCart(userId);
        
        cart.getItems().stream()
            .filter(item -> item.getFoodId().equals(foodId))
            .findFirst()
            .ifPresent(item -> {
                if (quantity <= 0) {
                    cart.getItems().remove(item);
                } else {
                    item.setQuantity(quantity);
                }
            });

        updateCartTotal(cart);
        cart.setUpdatedAt(getCurrentDateTime());
        
        return cartRepository.save(cart);
    }

    public Cart removeItemFromCart(String userId, String foodId) {
        Cart cart = getOrCreateCart(userId);
        
        cart.getItems().removeIf(item -> item.getFoodId().equals(foodId));
        
        updateCartTotal(cart);
        cart.setUpdatedAt(getCurrentDateTime());
        
        return cartRepository.save(cart);
    }

    public void clearCart(String userId) {
        Cart cart = getOrCreateCart(userId);
        cart.getItems().clear();
        cart.setTotalAmount(0);
        cart.setUpdatedAt(getCurrentDateTime());
        cartRepository.save(cart);
    }

    private void updateCartTotal(Cart cart) {
        double total = cart.getItems().stream()
            .mapToDouble(item -> item.getPrice() * item.getQuantity())
            .sum();
        cart.setTotalAmount(total);
    }
} 