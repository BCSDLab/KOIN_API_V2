package in.koreatech.koin.domain.order.shop.model.entity.delivery;

import static lombok.AccessLevel.PROTECTED;

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
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "orderable_shop_delivery_tip")
@NoArgsConstructor(access = PROTECTED)
public class OrderableShopDeliveryTip {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name ="campus_delivery_tip", nullable = false)
    private Integer campusDeliveryTip;

    @Column(name ="off_campus_delivery_tip", nullable = false)
    private Integer offCampusDeliveryTip;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderable_shop_id", nullable = false)
    private OrderableShop orderableShop;

    @Builder
    public OrderableShopDeliveryTip(Integer campusDeliveryTip, Integer offCampusDeliveryTip, OrderableShop orderableShop) {
        this.campusDeliveryTip = campusDeliveryTip;
        this.offCampusDeliveryTip = offCampusDeliveryTip;
        this.orderableShop = orderableShop;
    }

    public void updateDeliveryTip(Integer campusDeliveryTip, Integer offCampusDeliveryTip) {
        this.campusDeliveryTip = campusDeliveryTip;
        this.offCampusDeliveryTip = offCampusDeliveryTip;
    }
}
