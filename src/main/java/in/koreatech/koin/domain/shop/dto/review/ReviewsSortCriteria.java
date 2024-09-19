package in.koreatech.koin.domain.shop.dto.review;

import org.springframework.data.domain.Sort;

public enum ReviewsSortCriteria {

    LATEST("LATEST", Sort.by(Sort.Direction.DESC, "createdAt")),
    OLDEST("OLDEST", Sort.by(Sort.Direction.ASC, "createdAt")),
    HIGHEST_RATING("HIGHEST_RATING", Sort.by(Sort.Direction.DESC, "rating")),
    LOWEST_RATING("LOWEST_RATING", Sort.by(Sort.Direction.ASC, "rating")),
    ;

    private final String value;
    private final Sort sort;

    ReviewsSortCriteria(String value, Sort sort) {
        this.value = value;
        this.sort = sort;
    }

    public String getValue() {
        return value;
    }

    public Sort getSort() {
        return sort;
    }
}
