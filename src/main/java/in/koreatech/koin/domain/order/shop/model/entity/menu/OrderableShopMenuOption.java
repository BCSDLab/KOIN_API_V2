package in.koreatech.koin.domain.order.shop.model.entity.menu;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import org.hibernate.annotations.Where;

import in.koreatech.koin.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "orderable_shop_menu_option")
@Where(clause = "is_deleted=0")
@NoArgsConstructor(access = PROTECTED)
public class OrderableShopMenuOption extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderable_shop_menu_option_group_id", nullable = false)
    private OrderableShopMenuOptionGroup optionGroup;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "price", nullable = false)
    private Integer price;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Builder
    public OrderableShopMenuOption(OrderableShopMenuOptionGroup optionGroup, String name, Integer price,
        Boolean isDeleted) {
        this.optionGroup = optionGroup;
        this.name = name;
        this.price = price;
        this.isDeleted = isDeleted;
    }
}
