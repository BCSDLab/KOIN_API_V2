package in.koreatech.koin.domain.payment.repository;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.payment.model.redis.TemporaryPayment;

public interface TemporaryPaymentRedisRepository extends Repository<TemporaryPayment, String> {

    void save(TemporaryPayment temporaryPayment);
}
