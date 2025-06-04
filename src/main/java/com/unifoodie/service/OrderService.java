package com.unifoodie.service;

import com.unifoodie.model.Order;
import com.unifoodie.model.OrderItem;
import com.unifoodie.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartService cartService;

    private String getCurrentDateTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public List<Order> getOrdersByUserId(String userId) {
        System.out.println("🔍 OrderService - Searching for orders with userId: " + userId);

        List<Order> orders = orderRepository.findByUserId(userId);

        System.out.println("🔍 OrderService - Database query returned " + orders.size() + " orders");

        if (orders.isEmpty()) {
            System.out.println("⚠️ OrderService - No orders found in database for userId: " + userId);

            // Debug: Check total orders in database
            List<Order> allOrders = orderRepository.findAll();
            System.out.println("🔍 OrderService - Total orders in database: " + allOrders.size());

            if (!allOrders.isEmpty()) {
                System.out.println("🔍 OrderService - Sample userIds in database:");
                allOrders.stream()
                        .limit(5)
                        .forEach(order -> System.out.println("   - " + order.getUserId()));
            }
        }

        return orders;
    }

    public List<Order> getOrdersByUserIdAndStatus(String userId, String status) {
        return orderRepository.findByUserIdAndStatus(userId, status);
    }

    public Optional<Order> getOrderById(String id) {
        return orderRepository.findById(id);
    }

    public Order createOrder(String userId, List<OrderItem> items, String deliveryAddress,
            String paymentMethod, String specialInstructions) {
        Order order = new Order();
        order.setUserId(userId);
        order.setItems(items);
        order.setStatus("PENDING");
        order.setDeliveryAddress(deliveryAddress);
        order.setPaymentMethod(paymentMethod);
        order.setPaymentStatus("PENDING");
        order.setSpecialInstructions(specialInstructions);
        order.setCreatedAt(getCurrentDateTime());
        order.setUpdatedAt(getCurrentDateTime());

        // Calculate total amount
        double totalAmount = items.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
        order.setTotalAmount(totalAmount);

        // Clear the user's cart after order is created
        cartService.clearCart(userId);

        return orderRepository.save(order);
    }

    public Order updateOrderStatus(String id, String status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));

        order.setStatus(status);
        order.setUpdatedAt(getCurrentDateTime());

        return orderRepository.save(order);
    }

    public Order updatePaymentStatus(String id, String paymentStatus) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));

        order.setPaymentStatus(paymentStatus);
        order.setUpdatedAt(getCurrentDateTime());

        return orderRepository.save(order);
    }

    public void cancelOrder(String id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));

        if (!order.getStatus().equals("DELIVERED")) {
            order.setStatus("CANCELLED");
            order.setUpdatedAt(getCurrentDateTime());
            orderRepository.save(order);
        } else {
            throw new RuntimeException("Cannot cancel a delivered order");
        }
    }
}