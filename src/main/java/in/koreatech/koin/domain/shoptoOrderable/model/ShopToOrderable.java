package in.koreatech.koin.domain.shoptoOrderable.model;

import static lombok.AccessLevel.PROTECTED;

import in.koreatech.koin.domain.shop.model.shop.Shop;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "shop_to_orderable")
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor
@Builder
public class ShopToOrderable {
    // 임시 필드들 (추후 최종본에선 변경)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    private Shop shop;

    @Column(nullable = false)
    private Integer minimumOrderAmount;

    @Column(nullable = false)
    private Boolean takeout;

    @Column(nullable = false)
    private String deliveryOption;

    @Column(nullable = false)
    private Integer campusDeliveryTip;

    @Column(nullable = false)
    private Integer outsideDeliveryTip;

    @Column(nullable = false)
    private Boolean isOpen;

    @Column(nullable = false)
    private String businessLicenseUrl;

    @Column(nullable = false)
    private String businessCertificateUrl;

    @Column(nullable = false)
    private String bankCopyUrl;

    @Size(max = 10)
    @Column(name = "bank", length = 10)
    private String bank;

    @Size(max = 20)
    @Column(name = "account_number", length = 20)
    private String accountNumber;

    @Column(name = "image_urls", columnDefinition = "TEXT")
    private String imageUrls;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus requestStatus;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime approvedAt;

    public enum RequestStatus {
        PENDING, APPROVED, REJECTED
    }

    public static ShopToOrderable createRequest(
        Shop shop,
        Integer minimumOrderAmount,
        Boolean takeout,
        String deliveryOption,
        Integer campusDeliveryTip,
        Integer outsideDeliveryTip,
        Boolean isOpen,
        List<String> imageUrls,
        String businessLicenseUrl,
        String businessCertificateUrl,
        String bankCopyUrl,
        String bank,
        String accountNumber
    ) {
        String imageUrlsString = String.join(",", imageUrls);

        return ShopToOrderable.builder()
            .shop(shop)
            .minimumOrderAmount(minimumOrderAmount)
            .takeout(takeout)
            .deliveryOption(deliveryOption)
            .campusDeliveryTip(campusDeliveryTip)
            .outsideDeliveryTip(outsideDeliveryTip)
            .isOpen(isOpen)
            .businessLicenseUrl(businessLicenseUrl)
            .businessCertificateUrl(businessCertificateUrl)
            .bankCopyUrl(bankCopyUrl)
            .bank(bank)
            .accountNumber(accountNumber)
            .imageUrls(imageUrlsString)
            .requestStatus(RequestStatus.PENDING)
            .createdAt(LocalDateTime.now())
            .build();
    }

    public void approveRequest() {
        this.requestStatus = RequestStatus.APPROVED;
        this.approvedAt = LocalDateTime.now();
    }

    public void rejectRequest() {
        this.requestStatus = RequestStatus.REJECTED;
    }
}
