package in.koreatech.koin.domain.payment.repository;

import static in.koreatech.koin.global.code.ApiResponseCode.NOT_FOUND_PAYMENT;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.payment.model.entity.Payment;
import in.koreatech.koin.global.exception.CustomException;

public interface PaymentRepository extends Repository<Payment, Integer> {

    void save(Payment payment);

    Optional<Payment> findById(Integer id);

    default Payment getById(Integer id) {
        return findById(id)
            .orElseThrow(() -> CustomException.of(NOT_FOUND_PAYMENT));
    }

    Optional<Payment> findByOrderId(Integer orderId);

    default Payment getByOrderId(Integer orderId) {
        return findByOrderId(orderId)
            .orElseThrow(() -> CustomException.of(NOT_FOUND_PAYMENT));
    }
}
