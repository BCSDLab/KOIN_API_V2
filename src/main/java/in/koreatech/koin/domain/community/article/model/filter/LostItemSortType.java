package in.koreatech.koin.domain.community.article.model.filter;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LostItemSortType {
    LATEST("LATEST"),
    OLDEST("OLDEST");

    private final String value;
}
