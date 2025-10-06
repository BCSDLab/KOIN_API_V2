package in.koreatech.koin.domain.payment.repository;

import java.util.List;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.payment.model.entity.PaymentCancel;
import in.koreatech.koin.global.marker.JpaRepositoryMarker;

@JpaRepositoryMarker
public interface PaymentCancelRepository extends Repository<PaymentCancel, Integer> {

    void saveAll(Iterable<PaymentCancel> paymentCancels);

    List<PaymentCancel> findAllByPaymentId(Integer paymentId);
}
