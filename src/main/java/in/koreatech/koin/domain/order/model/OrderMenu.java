package in.koreatech.koin.domain.order.model;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(schema = "koin", name = "order_menu")
@NoArgsConstructor(access = PROTECTED)
public class OrderMenu {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @NotBlank
    @Column(name = "menu_name", nullable = false, updatable = false)
    private String menuName;

    @NotNull
    @Column(name = "menu_price", nullable = false, updatable = false)
    private Integer menuPrice;

    @Column(name = "menu_price_name", updatable = false)
    private String menuPriceName;

    @NotNull
    @Column(name = "quantity", nullable = false, updatable = false)
    private Integer quantity;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "order_id", nullable = false, updatable = false)
    private Order order;

    @OneToMany(mappedBy = "orderMenu", cascade = ALL, orphanRemoval = true)
    private List<OrderMenuOption> orderMenuOptions = new ArrayList<>();

    @Builder
    private OrderMenu(
        String menuName,
        String menuPriceName,
        Integer menuPrice,
        Integer quantity,
        Order order
    ) {
        this.menuName = menuName;
        this.menuPriceName = menuPriceName;
        this.menuPrice = menuPrice;
        this.quantity = quantity;
        this.order = order;
    }

    public void setOrderMenuOptions(List<OrderMenuOption> orderMenuOptions) {
        this.orderMenuOptions = orderMenuOptions;
    }
}
