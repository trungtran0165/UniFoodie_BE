package com.unifoodie.service;

import com.unifoodie.model.Report;
import com.unifoodie.repository.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private ReportRepository reportRepository;

    @Override
    public List<Report> getAllReports() {
        return reportRepository.findAll();
    }

    @Override
    public List<Report> getReportsByStoreId(String storeId) {
        return reportRepository.findByStoreId(storeId);
    }

    @Override
    public Optional<Report> getReportByStoreIdAndReportDate(String storeId, String reportDate) {
        return reportRepository.findByStoreIdAndReportDate(storeId, reportDate);
    }

    @Override
    public Report createReport(Report report) {
        return reportRepository.save(report);
    }

    @Override
    public Report updateReport(String id, Report reportDetails) {
        Report report = reportRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Report not found with id: " + id));

        report.setStoreId(reportDetails.getStoreId());
        report.setReportDate(reportDetails.getReportDate());
        report.setTotalOrders(reportDetails.getTotalOrders());
        report.setTotalRevenue(reportDetails.getTotalRevenue());
        report.setTopItems(reportDetails.getTopItems());

        return reportRepository.save(report);
    }

    @Override
    public void deleteReport(String id) {
        reportRepository.deleteById(id);
    }
} 