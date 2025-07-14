package in.koreatech.koin.domain.order.shop.model.entity.menu;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.Where;

import in.koreatech.koin._common.code.ApiResponseCode;
import in.koreatech.koin._common.exception.CustomException;
import in.koreatech.koin._common.model.BaseEntity;
import in.koreatech.koin.domain.order.shop.model.entity.shop.OrderableShop;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "orderable_shop_menu_option_group")
@Where(clause = "is_deleted=0")
@NoArgsConstructor(access = PROTECTED)
public class OrderableShopMenuOptionGroup extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderable_shop_id", nullable = false)
    private OrderableShop orderableShop;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "is_required", nullable = false)
    private Boolean isRequired = false;

    @Column(name = "min_select", nullable = false)
    private Integer minSelect = 0;

    @Column(name = "max_select")
    private Integer maxSelect;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @OneToMany(mappedBy = "optionGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderableShopMenuOption> menuOptions = new ArrayList<>();

    public void validateSelectionCount(int selectionCount) {
        // 필수 옵션 그룹인데 하나도 선택하지 않은 경우
        if (isRequired && selectionCount == 0) {
            throw CustomException.of(ApiResponseCode.REQUIRED_OPTION_GROUP_MISSING);
        }

        // 최소 선택 개수를 만족하지 못한 경우
        if (selectionCount < minSelect) {
            throw CustomException.of(ApiResponseCode.MIN_SELECTION_NOT_MET);
        }

        // 최대 선택 개수를 초과한 경우
        if (maxSelect != null && selectionCount > maxSelect) {
            throw CustomException.of(ApiResponseCode.MAX_SELECTION_EXCEEDED);
        }
    }
}
