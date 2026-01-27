package in.koreatech.koin.domain.community.article.model.filter;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LostItemCategoryFilter {

    ALL(null),
    CARD("카드"),
    ID("신분증"),
    WALLET("지갑"),
    ELECTRONICS("전자제품"),
    ETC("기타");

    private String status;
}
