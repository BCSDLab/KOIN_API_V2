package in.koreatech.koin.domain.order.delivery.model;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import java.time.LocalDateTime;

import in.koreatech.koin._common.exception.custom.KoinIllegalStateException;
import in.koreatech.koin._common.model.BaseEntity;
import in.koreatech.koin.domain.order.address.model.CampusDeliveryAddress;
import in.koreatech.koin.domain.user.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "user_delivery_address")
@NoArgsConstructor(access = PROTECTED)
public class UserDeliveryAddress extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "address_type", nullable = false, length = 20)
    private AddressType addressType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campus_delivery_address_id")
    private CampusDeliveryAddress campusDeliveryAddress;

    @Embedded
    private OffCampusDeliveryAddress offCampusDeliveryAddress;

    @Column(name = "last_used_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime lastUsedAt;

    @Column(name = "usage_count", nullable = false)
    private Integer usageCount = 0;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;

    public String getFullDeliveryAddress() {
        if (addressType == AddressType.CAMPUS) {
            return campusDeliveryAddress.getFullAddress();
        } else if (addressType == AddressType.OFF_CAMPUS) {
            return offCampusDeliveryAddress.getFullAddress();
        } else {
            throw new KoinIllegalStateException("부적절한 AddressType 입니다.");
        }
    }

    @Builder
    public UserDeliveryAddress(User user, AddressType addressType, CampusDeliveryAddress campusDeliveryAddress,
        OffCampusDeliveryAddress offCampusDeliveryAddress, LocalDateTime lastUsedAt, Integer usageCount,
        Boolean isDefault) {
        this.user = user;
        this.addressType = addressType;
        this.campusDeliveryAddress = campusDeliveryAddress;
        this.offCampusDeliveryAddress = offCampusDeliveryAddress;
        this.lastUsedAt = lastUsedAt;
        this.usageCount = usageCount;
        this.isDefault = isDefault;
    }

    public static UserDeliveryAddress ofOffCampus(User user, OffCampusDeliveryAddress offCampusDeliveryAddress) {
        return UserDeliveryAddress.builder()
            .user(user)
            .addressType(AddressType.OFF_CAMPUS)
            .offCampusDeliveryAddress(offCampusDeliveryAddress)
            .lastUsedAt(LocalDateTime.now())
            .build();
    }

    public static UserDeliveryAddress ofCampus(User user, CampusDeliveryAddress campusDeliveryAddress) {
        return UserDeliveryAddress.builder()
            .user(user)
            .addressType(AddressType.CAMPUS)
            .campusDeliveryAddress(campusDeliveryAddress)
            .lastUsedAt(LocalDateTime.now())
            .build();
    }
}
