package com.unifoodie.dto;

public class PaymentResponse {
    private String status;
    private String message;
    private String orderCode;
    private String paymentUrl;
    private String qrCode;

    // Constructors
    public PaymentResponse() {
    }

    public PaymentResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }

    // Static factory methods
    public static PaymentResponse success(String orderCode, String paymentUrl, String qrCode) {
        PaymentResponse response = new PaymentResponse("success", "Payment link created successfully");
        response.setOrderCode(orderCode);
        response.setPaymentUrl(paymentUrl);
        response.setQrCode(qrCode);
        return response;
    }

    public static PaymentResponse error(String message) {
        return new PaymentResponse("error", message);
    }

    // Getters and Setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public String getPaymentUrl() {
        return paymentUrl;
    }

    public void setPaymentUrl(String paymentUrl) {
        this.paymentUrl = paymentUrl;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }
}