package in.koreatech.koin.domain.shop.model.menu;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import java.util.ArrayList;
import java.util.List;

import in.koreatech.koin._common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "shop_menu_detail_group")
@NoArgsConstructor(access = PROTECTED)
public class MenuOptionGroup extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @Size(max = 255)
    @Column(name = "name")
    private String name;

    @Column(name = "is_required")
    private Boolean required;

    @Column(name = "min_select")
    private Integer minSelect = 0;

    @Column(name = "max_select")
    private Integer maxSelect = null;

    @OneToMany(fetch = FetchType.LAZY)
    private List<MenuOption> options = new ArrayList<>();

    @Builder
    private MenuOptionGroup(
        String name,
        Boolean required,
        Integer minSelect,
        Integer maxSelect,
        List<MenuOption> options
    ) {
        this.name = name;
        this.required = required;
        this.minSelect = minSelect;
        this.maxSelect = maxSelect;
        this.options = options;
    }
}
