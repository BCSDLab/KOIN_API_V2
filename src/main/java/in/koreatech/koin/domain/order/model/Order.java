package in.koreatech.koin.domain.order.model;

import static jakarta.persistence.FetchType.LAZY;
import static java.lang.Boolean.FALSE;
import static lombok.AccessLevel.PROTECTED;

import org.hibernate.annotations.Where;

import in.koreatech.koin._common.model.BaseEntity;
import in.koreatech.koin.domain.order.shop.model.entity.shop.OrderableShop;
import in.koreatech.koin.domain.user.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(schema = "koin", name = "order")
@Where(clause = "is_deleted=0")
@NoArgsConstructor(access = PROTECTED)
public class Order extends BaseEntity {

    @Id
    @NotBlank
    @Size(min = 6, max = 64)
    @Column(name = "id", length = 64, nullable = false, updatable = false)
    private String id;

    @NotBlank
    @Size(max = 100)
    @Column(name = "address", length = 100, nullable = false, updatable = false)
    private String address;

    @NotBlank
    @Size(max = 20)
    @Column(name = "phone_number", length = 20, nullable = false, updatable = false)
    private String phoneNumber;

    @NotNull
    @Size(max = 50)
    @Column(name = "to_owner", length = 50, nullable = false, updatable = false)
    private String toOwner;

    @Size(max = 50)
    @Column(name = "to_rider", length = 50, nullable = false, updatable = false)
    private String toRider;

    @NotNull
    @Column(name = "total_product_price", nullable = false, updatable = false)
    private Integer totalProductPrice;

    @NotNull
    @Column(name = "delivery_tip", nullable = false, updatable = false)
    private Integer deliveryTip;

    @NotNull
    @Column(name = "total_price", nullable = false, updatable = false)
    private Integer totalPrice;

    @NotNull
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = FALSE;

    @JoinColumn(name = "orderable_shop_id")
    @ManyToOne(fetch = LAZY)
    private OrderableShop orderableShop;

    @JoinColumn(name = "user_id")
    @ManyToOne(fetch = LAZY)
    private User user;
}
