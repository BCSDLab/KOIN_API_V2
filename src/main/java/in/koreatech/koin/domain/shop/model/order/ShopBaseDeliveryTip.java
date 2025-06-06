package in.koreatech.koin.domain.shop.model.order;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import org.hibernate.annotations.Where;

import in.koreatech.koin._common.model.BaseEntity;
import in.koreatech.koin.domain.shop.model.shop.Shop;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
@Table(name = "shop_base_delivery_tip")
@Where(clause = "is_deleted=0")
public class ShopBaseDeliveryTip extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @Column(name = "order_amount", nullable = false)
    private Integer orderAmount;

    @Column(name = "fee", nullable = false)
    private Integer fee;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted;
}
