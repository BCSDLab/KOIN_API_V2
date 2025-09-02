package in.koreatech.koin.domain.payment.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.payment.model.entity.PaymentIdempotencyKey;
import in.koreatech.koin.domain.payment.repository.PaymentIdempotencyKeyRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentIdempotencyKeyService {

    private final PaymentIdempotencyKeyRepository paymentIdempotencyKeyRepository;

    @Transactional
    public String getOrCreate(Integer userId) {
        return paymentIdempotencyKeyRepository
            .findByUserId(userId)
            .map(idempotencyKey -> {
                if (idempotencyKey.isOlderThanExpireDays()) {
                    idempotencyKey.updateIdempotencyKey(UUID.randomUUID().toString());
                }
                return idempotencyKey;
            })
            .orElseGet(() -> paymentIdempotencyKeyRepository.save(
                PaymentIdempotencyKey.builder()
                    .userId(userId)
                    .idempotencyKey(UUID.randomUUID().toString())
                    .build()
            )).getIdempotencyKey();
    }
}
