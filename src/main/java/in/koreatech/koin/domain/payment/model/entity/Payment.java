package in.koreatech.koin.domain.payment.model.entity;

import static in.koreatech.koin.domain.payment.model.entity.PaymentStatus.CANCELED;
import static in.koreatech.koin.global.code.ApiResponseCode.PAYMENT_ACCESS_DENIED;
import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static java.lang.Boolean.FALSE;
import static lombok.AccessLevel.PROTECTED;

import java.time.LocalDateTime;

import in.koreatech.koin.common.model.BaseEntity;
import in.koreatech.koin.domain.order.order.model.Order;
import in.koreatech.koin.domain.shop.model.shop.Shop;
import in.koreatech.koin.global.exception.CustomException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(schema = "koin", name = "payment_v2")
@NoArgsConstructor(access = PROTECTED)
public class Payment extends BaseEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @Size(max = 200)
    @Column(name = "payment_key", length = 200, updatable = false)
    private String paymentKey;

    @NotNull
    @Column(name = "amount", nullable = false, updatable = false)
    private Integer amount;

    @NotNull
    @Enumerated(STRING)
    @Column(name = "status", length = 30, nullable = false)
    private PaymentStatus paymentStatus;

    @NotNull
    @Enumerated(STRING)
    @Column(name = "method", length = 30, nullable = false, updatable = false)
    private PaymentMethod paymentMethod;

    @NotNull
    @Column(name = "description", length = 255, nullable = false, updatable = false)
    private String description;

    @Column(name = "easy_pay_company", length = 255, updatable = false)
    private String easyPayCompany;

    @NotNull
    @Column(name = "requested_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime requestedAt;

    @Column(name = "approved_at", updatable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime approvedAt;

    @Column(name = "receipt", updatable = false, columnDefinition = "TEXT")
    private String receipt;

    @Size(max = 10)
    @Column(name = "bank", length = 10, updatable = false)
    private String bank;

    @Size(max = 20)
    @Column(name = "account_number", length = 20, updatable = false)
    private String accountNumber;

    @NotNull
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = FALSE;

    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Builder
    public Payment(
        String paymentKey,
        Integer amount,
        PaymentStatus paymentStatus,
        PaymentMethod paymentMethod,
        String description,
        String easyPayCompany,
        LocalDateTime requestedAt,
        LocalDateTime approvedAt,
        String receipt,
        String bank,
        String accountNumber,
        Boolean isDeleted,
        Order order
    ) {
        this.paymentKey = paymentKey;
        this.amount = amount;
        this.paymentStatus = paymentStatus;
        this.paymentMethod = paymentMethod;
        this.description = description;
        this.easyPayCompany = easyPayCompany;
        this.requestedAt = requestedAt;
        this.approvedAt = approvedAt;
        this.receipt = receipt;
        this.bank = bank;
        this.accountNumber = accountNumber;
        this.isDeleted = isDeleted;
        this.order = order;
    }

    public static Payment createBankTransfer(Order order, LocalDateTime requestedAt) {
        Shop shop = order.getOrderableShop().getShop();
        String description = order.getOrderMenus().get(0).getMenuName();
        if (order.getOrderMenus().size() > 1) {
            description += " 외 " + (order.getOrderMenus().size() - 1) + "건";
        }
        return Payment.builder()
            .order(order)
            .amount(order.getTotalPrice())
            .paymentStatus(PaymentStatus.WAITING_FOR_DEPOSIT)
            .paymentMethod(PaymentMethod.ACCOUNT_TRANSFER)
            .description(description)
            .requestedAt(requestedAt)
            .bank(shop.getBank())
            .accountNumber(shop.getAccountNumber())
            .isDeleted(false)
            .build();
    }

    public boolean isDirectBankTransfer() {
        return paymentMethod == PaymentMethod.ACCOUNT_TRANSFER && paymentKey == null;
    }

    public void validateUserIdMatches(Integer userId) {
        if (!userId.equals(this.order.getUser().getId())) {
            throw CustomException.of(PAYMENT_ACCESS_DENIED);
        }
    }

    public void cancel(String reason, LocalDateTime canceledAt) {
        this.paymentStatus = CANCELED;
        this.order.cancel(reason, canceledAt);
    }
}
