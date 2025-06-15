package in.koreatech.koin.domain.shop.model.menu;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "shop_menu_detail_group_map")
@NoArgsConstructor(access = PROTECTED)
public class MenuOptionGroupMap {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_menu_id")
    private Menu menu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_menu_category_id")
    private MenuOptionGroup menuOptionGroup;

    @Builder
    private MenuOptionGroupMap(Menu menu, MenuOptionGroup menuOptionGroup) {
        this.menu = menu;
        this.menuOptionGroup = menuOptionGroup;
    }
}
