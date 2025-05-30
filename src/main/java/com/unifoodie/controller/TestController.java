package com.unifoodie.controller;

import com.unifoodie.model.User;
import com.unifoodie.model.MenuItem;
import com.unifoodie.service.UserService;
import com.unifoodie.service.MenuItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private UserService userService;

    @Autowired
    private MenuItemService menuItemService;

    @GetMapping("/hello")
    public ResponseEntity<Map<String, String>> hello() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Hello from UniFoodie API!");
        response.put("status", "success");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/db-connection")
    public ResponseEntity<Map<String, Object>> testDatabaseConnection() {
        Map<String, Object> response = new HashMap<>();
        try {
            // Test by counting users and menu items
            long userCount = userService.countUsers();
            long menuItemCount = menuItemService.countMenuItems();

            response.put("status", "success");
            response.put("message", "Database connection successful");
            response.put("userCount", userCount);
            response.put("menuItemCount", menuItemCount);
            response.put("database", "MongoDB Atlas");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Database connection failed: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/create-sample-data")
    public ResponseEntity<Map<String, String>> createSampleData() {
        Map<String, String> response = new HashMap<>();
        try {
            // Create sample menu item
            MenuItem sampleItem = new MenuItem();
            sampleItem.setName("Phở Bò");
            sampleItem.setDescription("Món phở truyền thống Việt Nam với thịt bò tươi ngon");
            sampleItem.setPrice(new BigDecimal("45000"));
            sampleItem.setCategory("Món chính");
            sampleItem.setImageUrl("https://example.com/pho-bo.jpg");
            sampleItem.setAvailable(true);

            menuItemService.createMenuItem(sampleItem);

            response.put("status", "success");
            response.put("message", "Sample data created successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to create sample data: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}