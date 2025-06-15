package in.koreatech.koin.domain.order.shop.model.entity.menu;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import org.hibernate.annotations.Where;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "orderable_shop_menu_option_group_map")
@Where(clause = "is_deleted=0")
@NoArgsConstructor(access = PROTECTED)
public class OrderableShopMenuOptionGroupMap {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderable_shop_menu_option_group_id", nullable = false)
    private OrderableShopMenuOptionGroup optionGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderable_shop_menu_id", nullable = false)
    private OrderableShopMenu menu;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

}
