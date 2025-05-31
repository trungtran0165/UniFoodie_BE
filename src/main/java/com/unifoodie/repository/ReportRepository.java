package com.unifoodie.repository;

import com.unifoodie.model.Report;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ReportRepository extends MongoRepository<Report, String> {
    List<Report> findByStoreId(String storeId);
    Optional<Report> findByStoreIdAndReportDate(String storeId, String reportDate);
} 