package in.koreatech.koin.domain.order.model;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import in.koreatech.koin._common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
    name = "payment_idempotency_key",
    uniqueConstraints = @UniqueConstraint(name = "uk_idempotency_key_user_id", columnNames = "user_id")
)
@NoArgsConstructor(access = PROTECTED)
public class PaymentIdempotencyKey extends BaseEntity {

    private static final int EXPIRE_DAYS = 15;

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @NotNull
    @Column(name = "user_id", nullable = false, updatable = false)
    private Integer userId;

    @NotNull
    @Size(max = 300)
    @Column(name = "idempotency_key",length = 300, nullable = false)
    private String idempotencyKey;

    @Builder
    private PaymentIdempotencyKey(
        Integer userId,
        String idempotencyKey
    ) {
        this.userId = userId;
        this.idempotencyKey = idempotencyKey;
    }

    public void updateIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public boolean isOlderThanExpireDays() {
        return ChronoUnit.DAYS.between(super.getUpdatedAt(), LocalDateTime.now()) >= EXPIRE_DAYS;
    }
}
