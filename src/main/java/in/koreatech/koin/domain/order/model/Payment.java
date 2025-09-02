package in.koreatech.koin.domain.order.model;

import static in.koreatech.koin.domain.order.model.PaymentStatus.CANCELED;
import static in.koreatech.koin.global.code.ApiResponseCode.PAYMENT_ACCESS_DENIED;
import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import java.time.LocalDateTime;

import in.koreatech.koin.global.exception.CustomException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(schema = "koin", name = "payment")
@NoArgsConstructor(access = PROTECTED)
public class Payment {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @NotBlank
    @Size(max = 200)
    @Column(name = "payment_key", length = 200, nullable = false, updatable = false)
    private String paymentKey;

    @NotNull
    @Column(name = "amount", nullable = false, updatable = false)
    private Integer amount;

    @NotNull
    @Size(max = 30)
    @Enumerated(STRING)
    @Column(name = "status", length = 30, nullable = false)
    private PaymentStatus paymentStatus;

    @NotNull
    @Size(max = 30)
    @Enumerated(STRING)
    @Column(name = "method", length = 30, nullable = false, updatable = false)
    private PaymentMethod paymentMethod;

    @NotNull
    @Column(name = "requested_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime requestedAt;

    @NotNull
    @Column(name = "approved_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime approvedAt;

    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Builder
    private Payment(
        String paymentKey,
        Integer amount,
        PaymentStatus paymentStatus,
        PaymentMethod paymentMethod,
        LocalDateTime requestedAt,
        LocalDateTime approvedAt,
        Order order
    ) {
        this.paymentKey = paymentKey;
        this.amount = amount;
        this.paymentStatus = paymentStatus;
        this.paymentMethod = paymentMethod;
        this.requestedAt = requestedAt;
        this.approvedAt = approvedAt;
        this.order = order;
    }

    public void validateUserIdMatches(Integer userId) {
        if (!userId.equals(this.order.getUser().getId())) {
            throw CustomException.of(PAYMENT_ACCESS_DENIED);
        }
    }

    public void cancel() {
        this.paymentStatus = CANCELED;
    }
}
