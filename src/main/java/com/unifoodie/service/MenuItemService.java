package com.unifoodie.service;

import com.unifoodie.model.MenuItem;
import com.unifoodie.repository.MenuItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class MenuItemService {
    @Autowired
    private MenuItemRepository menuItemRepository;

    public List<MenuItem> getAllMenuItems() {
        return menuItemRepository.findAll();
    }

    public Optional<MenuItem> getMenuItemById(String id) {
        return menuItemRepository.findById(id);
    }

    @Transactional
    public MenuItem createMenuItem(MenuItem menuItem) {
        return menuItemRepository.save(menuItem);
    }

    @Transactional
    public MenuItem updateMenuItem(String id, MenuItem menuItemDetails) {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("MenuItem not found with id: " + id));
        menuItem.setName(menuItemDetails.getName());
        menuItem.setDescription(menuItemDetails.getDescription());
        menuItem.setPrice(menuItemDetails.getPrice());
        menuItem.setImageUrl(menuItemDetails.getImageUrl());
        menuItem.setCategory(menuItemDetails.getCategory());
        return menuItemRepository.save(menuItem);
    }

    @Transactional
    public MenuItem toggleAvailable(String id) {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("MenuItem not found with id: " + id));
        menuItem.setAvailable(!menuItem.isAvailable());
        return menuItemRepository.save(menuItem);
    }

    @Transactional
    public void deleteMenuItem(String id) {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("MenuItem not found with id: " + id));
        menuItemRepository.delete(menuItem);
    }

    public long countMenuItems() {
        return menuItemRepository.count();
    }
}