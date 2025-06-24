package in.koreatech.koin.domain.order.shop.model.entity.delivery;

import static lombok.AccessLevel.PROTECTED;

import in.koreatech.koin._common.model.BaseEntity;
import in.koreatech.koin.domain.order.shop.model.entity.shop.OrderableShop;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "orderable_shop_delivery_option")
@NoArgsConstructor(access = PROTECTED)
public class OrderableShopDeliveryOption extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "campus_delivery", nullable = false)
    private Boolean campusDelivery;

    @Column(name = "off_campus_delivery", nullable = false)
    private Boolean offCampusDelivery;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderable_shop_id", nullable = false)
    private OrderableShop orderableShop;

}
