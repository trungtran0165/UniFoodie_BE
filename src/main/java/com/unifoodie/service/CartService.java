package com.unifoodie.service;

import com.unifoodie.model.Cart;
import com.unifoodie.model.CartItem;
import com.unifoodie.model.Food;
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

    @Autowired
    private FoodService foodService;

    private String getCurrentDateTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
    }

    public Cart getOrCreateCart(String userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUserId(userId);
                    newCart.setCreatedAt(getCurrentDateTime());
                    newCart.setUpdatedAt(getCurrentDateTime());
                    return cartRepository.save(newCart);
                });

        // Populate food information for existing cart items
        populateCartItemsWithFoodInfo(cart);

        return cart;
    }

    public Cart addItemToCart(String userId, CartItem item) {
        Cart cart = getOrCreateCart(userId);

        // Populate food information from database using foodId
        Optional<Food> foodOpt = foodService.getFoodById(item.getFoodId());
        if (foodOpt.isEmpty()) {
            throw new RuntimeException("Food not found with id: " + item.getFoodId());
        }

        Food food = foodOpt.get();

        // Populate cart item with complete food information
        item.setName(food.getName());
        item.setPrice(food.getPrice());
        item.setImage(food.getImage());

        System.out.println("🛒 Adding item to cart - Food ID: " + item.getFoodId() +
                ", Name: " + item.getName() +
                ", Price: " + item.getPrice() +
                ", ObjectId: " + food.get_id());

        // Check if item already exists in cart
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(i -> i.getFoodId().equals(item.getFoodId()))
                .findFirst();

        if (existingItem.isPresent()) {
            // Update quantity if item exists
            existingItem.get().setQuantity(existingItem.get().getQuantity() + item.getQuantity());
            System.out.println("🔄 Updated existing item quantity to: " + existingItem.get().getQuantity());
        } else {
            // Add new item if it doesn't exist
            cart.getItems().add(item);
            System.out.println("➕ Added new item to cart");
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
                        System.out.println("🗑️ Removed item from cart: " + foodId);
                    } else {
                        item.setQuantity(quantity);
                        System.out.println("🔄 Updated item quantity: " + foodId + " -> " + quantity);
                    }
                });

        updateCartTotal(cart);
        cart.setUpdatedAt(getCurrentDateTime());

        return cartRepository.save(cart);
    }

    public Cart removeItemFromCart(String userId, String foodId) {
        Cart cart = getOrCreateCart(userId);

        boolean removed = cart.getItems().removeIf(item -> item.getFoodId().equals(foodId));

        if (removed) {
            System.out.println("🗑️ Removed item from cart: " + foodId);
        } else {
            System.out.println("⚠️ Item not found in cart: " + foodId);
        }

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
        System.out.println("🧹 Cleared cart for user: " + userId);
    }

    private void updateCartTotal(Cart cart) {
        double total = cart.getItems().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
        cart.setTotalAmount(total);
        System.out.println("💰 Updated cart total: " + total);
    }

    private void populateCartItemsWithFoodInfo(Cart cart) {
        for (CartItem item : cart.getItems()) {
            // Only populate if item is missing essential info
            if (item.getName() == null || item.getName().isEmpty() || item.getPrice() == 0) {
                Optional<Food> foodOpt = foodService.getFoodById(item.getFoodId());
                if (foodOpt.isPresent()) {
                    Food food = foodOpt.get();
                    item.setName(food.getName());
                    item.setPrice(food.getPrice());
                    item.setImage(food.getImage());
                    System.out.println("🔄 Populated cart item info for: " + food.getName());
                } else {
                    System.err.println("⚠️ Food not found for cart item with foodId: " + item.getFoodId());
                }
            }
        }
    }
}