// package com.unifoodie.controller;

// import com.unifoodie.model.Promotion;
// import com.unifoodie.service.PromotionService;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// import java.util.List;

// @RestController
// @RequestMapping("/api/promotions")
// @CrossOrigin(origins = "*")
// public class PromotionController {
//     @Autowired
//     private PromotionService promotionService;

//     @PostMapping
//     public ResponseEntity<Promotion> createPromotion(@RequestBody Promotion promotion) {
//         return ResponseEntity.ok(promotionService.createPromotion(promotion));
//     }

//     @PutMapping("/{id}")
//     public ResponseEntity<Promotion> updatePromotion(@PathVariable Long id, @RequestBody Promotion promotion) {
//         return ResponseEntity.ok(promotionService.updatePromotion(id, promotion));
//     }

//     @DeleteMapping("/{id}")
//     public ResponseEntity<Void> deletePromotion(@PathVariable Long id) {
//         promotionService.deletePromotion(id);
//         return ResponseEntity.ok().build();
//     }

//     @GetMapping
//     public ResponseEntity<List<Promotion>> getAllPromotions() {
//         return ResponseEntity.ok(promotionService.getAllPromotions());
//     }

//     @GetMapping("/{id}")
//     public ResponseEntity<Promotion> getPromotionById(@PathVariable Long id) {
//         return promotionService.getPromotionById(id)
//                 .map(ResponseEntity::ok)
//                 .orElse(ResponseEntity.notFound().build());
//     }
// } 