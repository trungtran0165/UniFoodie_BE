package com.unifoodie.service;

import com.unifoodie.model.Report;

import java.util.List;
import java.util.Optional;

public interface ReportService {
    List<Report> getAllReports();
    List<Report> getReportsByStoreId(String storeId);
    Optional<Report> getReportByStoreIdAndReportDate(String storeId, String reportDate);
    Report createReport(Report report);
    Report updateReport(String id, Report reportDetails);
    void deleteReport(String id);
} 