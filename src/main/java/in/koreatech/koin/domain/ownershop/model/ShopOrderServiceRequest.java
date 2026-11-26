package in.koreatech.koin.domain.ownershop.model;

import static in.koreatech.koin.domain.ownershop.model.ShopOrderServiceRequestStatus.*;
import static in.koreatech.koin.global.code.ApiResponseCode.ORDER_SERVICE_REQUEST_ALREADY_PROCESSED;
import static lombok.AccessLevel.PROTECTED;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import in.koreatech.koin.common.model.BaseEntity;
import in.koreatech.koin.domain.shop.model.shop.Shop;
import in.koreatech.koin.global.exception.CustomException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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
@Table(name = "shop_order_service_requests")
@NoArgsConstructor(access = PROTECTED)
public class ShopOrderServiceRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @NotNull
    @Column(name = "minimum_order_amount", nullable = false)
    private Integer minimumOrderAmount;

    @Column(name = "is_takeout", nullable = false)
    private Boolean isTakeout = false;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_option", nullable = false)
    private ShopOrderServiceRequestDeliveryOption deliveryOption;

    @Column(name = "campus_delivery_tip", nullable = false)
    private Integer campusDeliveryTip = 0;

    @Column(name = "off_campus_delivery_tip", nullable = false)
    private Integer offCampusDeliveryTip = 0;

    @NotNull
    @Column(name = "business_license_url", nullable = false)
    private String businessLicenseUrl;

    @NotNull
    @Column(name = "business_certificate_url", nullable = false)
    private String businessCertificateUrl;

    @NotNull
    @Column(name = "bank_copy_url", nullable = false)
    private String bankCopyUrl;

    @NotNull
    @Size(max = 10)
    @Column(name = "bank", length = 10, nullable = false)
    private String bank;

    @NotNull
    @Size(max = 20)
    @Column(name = "account_number", length = 20, nullable = false)
    private String accountNumber;

    @Size(max = 50)
    @Column(name = "account_holder", length = 50)
    private String accountHolder;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "menu_snapshot", columnDefinition = "JSON", updatable = false)
    private List<ShopOrderServiceMenuSnapshot> menus;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_status", nullable = false)
    private ShopOrderServiceRequestStatus requestStatus = ShopOrderServiceRequestStatus.PENDING;

    @Column(name = "approved_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime approvedAt = null;

    @Size(max = 500)
    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Builder
    public ShopOrderServiceRequest(
        Shop shop,
        Integer minimumOrderAmount,
        Boolean isTakeout,
        ShopOrderServiceRequestDeliveryOption deliveryOption,
        Integer campusDeliveryTip,
        Integer offCampusDeliveryTip,
        String businessLicenseUrl,
        String businessCertificateUrl,
        String bankCopyUrl,
        String bank,
        String accountNumber,
        String accountHolder,
        List<ShopOrderServiceMenuSnapshot> menus
    ) {
        this.shop = shop;
        this.minimumOrderAmount = minimumOrderAmount;
        this.isTakeout = isTakeout;
        this.deliveryOption = deliveryOption;
        this.campusDeliveryTip = campusDeliveryTip;
        this.offCampusDeliveryTip = offCampusDeliveryTip;
        this.businessLicenseUrl = businessLicenseUrl;
        this.businessCertificateUrl = businessCertificateUrl;
        this.bankCopyUrl = bankCopyUrl;
        this.bank = bank;
        this.accountNumber = accountNumber;
        this.accountHolder = accountHolder;
        this.menus = menus;
        this.approvedAt = null;
    }

    public void requirePending() {
        if (requestStatus != PENDING) {
            throw CustomException.of(ORDER_SERVICE_REQUEST_ALREADY_PROCESSED);
        }
    }

    public void approve(LocalDateTime approvedAt) {
        requirePending();
        this.requestStatus = APPROVED;
        this.approvedAt = approvedAt;
    }

    public void reject(String rejectionReason) {
        requirePending();
        this.requestStatus = REJECTED;
        this.rejectionReason = rejectionReason;
    }

}
