package in.koreatech.koin.domain.order.model;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "payment_cancel")
@NoArgsConstructor(access = PROTECTED)
public class PaymentCancel {

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
    @Column(name = "cancel_reason", length = 200, updatable = false, nullable = false)
    private String cancelReason;

    @NotNull
    @Column(name = "cancel_amount", updatable = false, nullable = false)
    private Integer cancelAmount;

    @NotNull
    @Column(name = "canceled_at", updatable = false, nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime canceledAt;

    @JoinColumn(name = "payment_id")
    @ManyToOne(fetch = LAZY)
    private Payment payment;
}
