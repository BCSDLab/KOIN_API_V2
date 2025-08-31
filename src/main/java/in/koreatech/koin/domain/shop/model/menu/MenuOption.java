package in.koreatech.koin.domain.shop.model.menu;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import in.koreatech.koin.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "shop_menu_details", uniqueConstraints = {
    @UniqueConstraint(
        name = "SHOP_MENU_ID_AND_OPTION_AND_PRICE",
        columnNames = {"shop_menu_id", "option", "price"}
    )}
)
@NoArgsConstructor(access = PROTECTED)
public class MenuOption extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_menu_id")
    private Menu menu;

    @Size(max = 255)
    @Column(name = "`option`")
    private String option;

    @NotNull
    @Column(name = "price", nullable = false)
    @PositiveOrZero
    private Integer price;

    @Builder
    private MenuOption(String option, Integer price, Menu menu) {
        this.option = option;
        this.price = price;
        this.menu = menu;
    }

    public void setMenu(Menu menu) {
        if (menu.equals(this.menu)) {
            return;
        }

        if (this.menu != null) {
            this.menu.getMenuOptions().remove(this);
        }
        this.menu = menu;
        this.menu.getMenuOptions().add(this);
    }
}
