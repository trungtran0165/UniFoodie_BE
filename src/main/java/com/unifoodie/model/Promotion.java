package com.unifoodie.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;
import java.util.List;

@Document(collection = "promotions")
public class Promotion {

    @Id
    private String id;

    private String name;

    private String description;

    private String type; // e.g., "PERCENTAGE", "FIXED_AMOUNT", "BUY_X_GET_Y"

    private double value; // Discount percentage or fixed amount

    private Date startDate;

    private Date endDate;

    private boolean active = true; // Whether the promotion is currently active

    // Optional: list of food IDs or category IDs this promotion applies to
    private List<String> applicableFoodIds;
    private List<String> applicableCategoryIds;

    // Manual Constructors (if not using Lombok)
    public Promotion() {
    }

    public Promotion(String id, String name, String description, String type, double value, Date startDate, Date endDate, boolean active, List<String> applicableFoodIds, List<String> applicableCategoryIds) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.value = value;
        this.startDate = startDate;
        this.endDate = endDate;
        this.active = active;
        this.applicableFoodIds = applicableFoodIds;
        this.applicableCategoryIds = applicableCategoryIds;
    }

    // Manual Getters and Setters (if not using Lombok)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<String> getApplicableFoodIds() {
        return applicableFoodIds;
    }

    public void setApplicableFoodIds(List<String> applicableFoodIds) {
        this.applicableFoodIds = applicableFoodIds;
    }

    public List<String> getApplicableCategoryIds() {
        return applicableCategoryIds;
    }

    public void setApplicableCategoryIds(List<String> applicableCategoryIds) {
        this.applicableCategoryIds = applicableCategoryIds;
    }
} 