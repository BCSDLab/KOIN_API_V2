package in.koreatech.koin.domain.payment.repository;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.order.model.Payment;

public interface PaymentRepository extends Repository<Payment, Integer> {

    void save(Payment payment);
}
