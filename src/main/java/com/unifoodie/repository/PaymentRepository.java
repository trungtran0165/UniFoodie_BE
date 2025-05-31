package com.unifoodie.repository;

import com.unifoodie.model.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface PaymentRepository extends MongoRepository<Payment, String> {

    Optional<Payment> findByOrderCode(String orderCode);

    Optional<Payment> findByOrderId(String orderId);

    List<Payment> findByStatus(String status);

    List<Payment> findByCustomerEmail(String customerEmail);

    List<Payment> findByOrderIdAndStatus(String orderId, String status);
}