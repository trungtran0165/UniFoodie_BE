package com.unifoodie.service;

import com.unifoodie.model.Promotion;
import com.unifoodie.repository.PromotionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PromotionService {
    @Autowired
    private PromotionRepository promotionRepository;

    public List<Promotion> getAllPromotions() {
        return promotionRepository.findAll();
    }

    public Optional<Promotion> getPromotionById(String id) {
        return promotionRepository.findById(id);
    }

    @Transactional
    public Promotion createPromotion(Promotion promotion) {
        return promotionRepository.save(promotion);
    }

    @Transactional
    public Promotion updatePromotion(String id, Promotion promotionDetails) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion not found with id: " + id));
        promotion.setName(promotionDetails.getName());
        promotion.setValue(promotionDetails.getValue());
        promotion.setStartDate(promotionDetails.getStartDate());
        promotion.setEndDate(promotionDetails.getEndDate());
        promotion.setActive(promotionDetails.isActive());
        return promotionRepository.save(promotion);
    }

    @Transactional
    public void deletePromotion(String id) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion not found with id: " + id));
        promotionRepository.delete(promotion);
    }

    @Transactional
    public Promotion patchPromotion(String id, Promotion promotionDetails) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion not found with id: " + id));
        if (promotionDetails.getName() != null) promotion.setName(promotionDetails.getName());
        if (promotionDetails.getValue() != 0) promotion.setValue(promotionDetails.getValue());
        if (promotionDetails.getStartDate() != null) promotion.setStartDate(promotionDetails.getStartDate());
        if (promotionDetails.getEndDate() != null) promotion.setEndDate(promotionDetails.getEndDate());
        promotion.setActive(promotionDetails.isActive());
        return promotionRepository.save(promotion);
    }
} 