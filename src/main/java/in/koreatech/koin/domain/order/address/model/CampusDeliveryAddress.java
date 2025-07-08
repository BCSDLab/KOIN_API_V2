package in.koreatech.koin.domain.order.address.model;

import static lombok.AccessLevel.PROTECTED;

import java.math.BigDecimal;

import in.koreatech.koin._common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "campus_delivery_address")
@NoArgsConstructor(access = PROTECTED)
public class CampusDeliveryAddress extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "full_address", nullable = false, length = 255)
    private String fullAddress;

    @Column(name = "short_address", nullable = false, length = 50)
    private String shortAddress;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campus_address_type_id", nullable = false)
    private CampusDeliveryAddressType campusAddressType;

    @Column(name = "latitude", nullable = false, precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(name = "longitude", nullable = false,precision = 11, scale = 8)
    private BigDecimal longitude;
}
