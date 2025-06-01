package com.unifoodie.controller;

import com.unifoodie.dto.CreateOrderRequest;
import com.unifoodie.model.Order;
import com.unifoodie.model.OrderItem;
import com.unifoodie.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * NEW ENDPOINT: Create order with DTO approach (all data in request body)
     */
    @PostMapping("/create")
    public ResponseEntity<?> createOrderWithBody(@RequestBody CreateOrderRequest request) {
        try {
            System.out.println("📝 Received order request: " + request.toString());

            // Validation
            if (!request.isValid()) {
                String errorMsg = "Invalid request: userId, items, deliveryAddress, and paymentMethod are required";
                System.err.println("❌ Validation failed: " + errorMsg);
                return ResponseEntity.badRequest().body(errorMsg);
            }

            // Create order
            Order createdOrder = orderService.createOrder(
                    request.getUserId(),
                    request.getItems(),
                    request.getDeliveryAddress(),
                    request.getPaymentMethod(),
                    request.getSpecialInstructions());

            // Success logging
            System.out.println("✅ Order created successfully!");
            System.out.println("   Order ID: " + createdOrder.getId());
            System.out.println("   User ID: " + createdOrder.getUserId());
            System.out.println("   Items: " + createdOrder.getItems().size());
            System.out.println("   Total Amount: " + createdOrder.getTotalAmount() + "đ");
            System.out.println("   Status: " + createdOrder.getStatus());
            System.out.println("   Payment Status: " + createdOrder.getPaymentStatus());
            System.out.println("   🤖 Order is now available for AI recommendation!");

            return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);

        } catch (Exception e) {
            System.err.println("❌ Order creation failed: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body("Order creation failed: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Order>> getOrdersByUserId(@PathVariable String userId) {
        List<Order> orders = orderService.getOrdersByUserId(userId);
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    @GetMapping("/user/{userId}/status/{status}")
    public ResponseEntity<List<Order>> getOrdersByUserIdAndStatus(
            @PathVariable String userId,
            @PathVariable String status) {
        List<Order> orders = orderService.getOrdersByUserIdAndStatus(userId, status);
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable String id) {
        Optional<Order> order = orderService.getOrderById(id);
        return order.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping("/{userId}")
    public ResponseEntity<Order> createOrder(
            @PathVariable String userId,
            @RequestBody List<OrderItem> items,
            @RequestParam String deliveryAddress,
            @RequestParam String paymentMethod,
            @RequestParam(required = false) String specialInstructions) {
        Order createdOrder = orderService.createOrder(userId, items, deliveryAddress, paymentMethod,
                specialInstructions);
        return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable String id, @RequestParam String status) {
        try {
            Order updatedOrder = orderService.updateOrderStatus(id, status);
            return new ResponseEntity<>(updatedOrder, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}/payment-status")
    public ResponseEntity<?> updatePaymentStatus(@PathVariable String id, @RequestParam String paymentStatus) {
        try {
            Order updatedOrder = orderService.updatePaymentStatus(id, paymentStatus);
            return new ResponseEntity<>(updatedOrder, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelOrder(@PathVariable String id) {
        try {
            orderService.cancelOrder(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}