package in.koreatech.koin.domain.shop.model.menu;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import in.koreatech.koin._common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "shop_menu_search_keywords")
@NoArgsConstructor(access = PROTECTED)
public class MenuSearchKeyWord extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @Size(max = 255)
    @NotNull
    @Column(name = "keyword", nullable = false)
    private String keyword;

    @Builder
    private MenuSearchKeyWord(
        String keyword
    ) {
        this.keyword = keyword;
    }
}
