package in.koreatech.koin.domain.payment.model.entity;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static java.lang.Boolean.FALSE;
import static lombok.AccessLevel.PROTECTED;

import java.time.LocalDateTime;

import in.koreatech.koin.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "payment_cancel_v2")
@NoArgsConstructor(access = PROTECTED)
public class PaymentCancel extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Integer id;

    @NotNull
    @Size(max = 64)
    @Column(name = "transaction_key", length = 64, updatable = false, nullable = false)
    private String transactionKey;

    @NotNull
    @Size(max = 200)
    @Column(name = "reason", length = 200, updatable = false, nullable = false)
    private String cancelReason;

    @NotNull
    @Column(name = "amount", updatable = false, nullable = false)
    private Integer cancelAmount;

    @NotNull
    @Column(name = "canceled_at", updatable = false, nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime canceledAt;

    @NotNull
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = FALSE;

    @JoinColumn(name = "payment_id")
    @ManyToOne(fetch = LAZY)
    private Payment payment;

    @Builder
    private PaymentCancel(
        String transactionKey,
        String cancelReason,
        Integer cancelAmount,
        LocalDateTime canceledAt,
        Boolean isDeleted, Payment payment
    ) {
        this.transactionKey = transactionKey;
        this.cancelReason = cancelReason;
        this.cancelAmount = cancelAmount;
        this.canceledAt = canceledAt;
        this.isDeleted = isDeleted;
        this.payment = payment;
    }
}
