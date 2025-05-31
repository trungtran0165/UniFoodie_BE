package com.unifoodie.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

// Assuming Lombok is configured, otherwise add getters/setters/constructors manually
// import lombok.Data;
// import lombok.NoArgsConstructor;
// import lombok.AllArgsConstructor;

@Document(collection = "reports")
// @Data
// @NoArgsConstructor
// @AllArgsConstructor
public class Report {

    @Id
    private String id; // Corresponds to report_id

    private String storeId; // Corresponds to store_id (FK - assuming store ID is String)

    private String reportDate; // Corresponds to report_date (Using String for simplicity, could be Date)

    private int totalOrders; // Corresponds to total_orders

    private double totalRevenue; // Corresponds to total_revenue

    private String topItems; // Corresponds to top_items (Using String, could be more complex)

    // Manual Constructors (if not using Lombok)
    public Report() {
    }

    public Report(String id, String storeId, String reportDate, int totalOrders, double totalRevenue, String topItems) {
        this.id = id;
        this.storeId = storeId;
        this.reportDate = reportDate;
        this.totalOrders = totalOrders;
        this.totalRevenue = totalRevenue;
        this.topItems = topItems;
    }

    // Manual Getters and Setters (if not using Lombok)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public String getReportDate() {
        return reportDate;
    }

    public void setReportDate(String reportDate) {
        this.reportDate = reportDate;
    }

    public int getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(int totalOrders) {
        this.totalOrders = totalOrders;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public String getTopItems() {
        return topItems;
    }

    public void setTopItems(String topItems) {
        this.topItems = topItems;
    }
} 