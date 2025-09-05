package in.koreatech.koin.domain.order.model;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(schema = "koin", name = "order_menu_option")
@NoArgsConstructor(access = PROTECTED)
public class OrderMenuOption {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Integer id;

    @NotBlank
    @Column(name = "option_group_name", nullable = false, updatable = false)
    private String optionGroupName;

    @NotBlank
    @Column(name = "option_name", nullable = false, updatable = false)
    private String optionName;

    @NotNull
    @Column(name = "option_price", nullable = false, updatable = false)
    private Integer optionPrice;

    @NotNull
    @Column(name = "quantity", nullable = false, updatable = false)
    private Integer quantity;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "order_menu_id", nullable = false)
    private OrderMenu orderMenu;

    @Builder
    private OrderMenuOption(
        String optionGroupName,
        String optionName,
        Integer optionPrice,
        Integer quantity,
        OrderMenu orderMenu
    ) {
        this.optionGroupName = optionGroupName;
        this.optionName = optionName;
        this.optionPrice = optionPrice;
        this.quantity = quantity;
        this.orderMenu = orderMenu;
    }
}
