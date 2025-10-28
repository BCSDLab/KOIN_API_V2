package in.koreatech.koin.domain.shoptoOrderable.model;

import static lombok.AccessLevel.PROTECTED;

import in.koreatech.koin.common.model.BaseEntity;
import in.koreatech.koin.domain.shop.model.shop.Shop;
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

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "shop_to_orderable")
@NoArgsConstructor(access = PROTECTED)
public class ShopToOrderable extends BaseEntity {

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
    private ShopToOrderableDeliveryOption deliveryOption;

    @Column(name = "campus_delivery_tip", nullable = false)
    private Integer campusDeliveryTip = 0;

    @Column(name = "outside_delivery_tip", nullable = false)
    private Integer outsideDeliveryTip = 0;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "request_status", nullable = false)
    private ShopToOrderableRequestStatus requestStatus = ShopToOrderableRequestStatus.PENDING;

    @Column(name = "approved_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime approvedAt = null;

    @Builder
    public ShopToOrderable(
        Shop shop,
        Integer minimumOrderAmount,
        Boolean isTakeout,
        ShopToOrderableDeliveryOption deliveryOption,
        Integer campusDeliveryTip,
        Integer outsideDeliveryTip,
        String businessLicenseUrl,
        String businessCertificateUrl,
        String bankCopyUrl,
        String bank,
        String accountNumber
    ) {
        this.shop = shop;
        this.minimumOrderAmount = minimumOrderAmount;
        this.isTakeout = isTakeout;
        this.deliveryOption = deliveryOption;
        this.campusDeliveryTip = campusDeliveryTip;
        this.outsideDeliveryTip = outsideDeliveryTip;
        this.businessLicenseUrl = businessLicenseUrl;
        this.businessCertificateUrl = businessCertificateUrl;
        this.bankCopyUrl = bankCopyUrl;
        this.bank = bank;
        this.accountNumber = accountNumber;
        this.approvedAt = null;
    }

    public void approveRequest() {
        this.requestStatus = ShopToOrderableRequestStatus.APPROVED;
        this.approvedAt = LocalDateTime.now();
    }

    public void rejectRequest() {
        this.requestStatus = ShopToOrderableRequestStatus.REJECTED;
    }
}
