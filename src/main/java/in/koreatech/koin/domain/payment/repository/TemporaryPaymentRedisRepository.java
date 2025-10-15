package in.koreatech.koin.domain.payment.repository;

import static in.koreatech.koin.global.code.ApiResponseCode.NOT_FOUND_TEMPORARY_PAYMENT;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.payment.model.redis.TemporaryPayment;
import in.koreatech.koin.global.exception.CustomException;

public interface TemporaryPaymentRedisRepository extends Repository<TemporaryPayment, String> {

    void save(TemporaryPayment temporaryPayment);

    Optional<TemporaryPayment> findById(String pgOrderId);

    default TemporaryPayment getById(String pgOrderId) {
        return findById(pgOrderId)
            .orElseThrow(() -> CustomException.of(NOT_FOUND_TEMPORARY_PAYMENT));
    }

    void deleteById(String pgOrderId);
}
