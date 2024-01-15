package in.koreatech.koin.domain.community.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Criteria {
    private static final Long DEFAULT_PAGE = 1L;
    private static final Long MIN_PAGE = 1L;

    private static final Long DEFAULT_LIMIT = 10L;
    private static final Long MIN_LIMIT = 1L;
    private static final Long MAX_LIMIT = 50L;

    private final int page;
    private final int limit;

    public static Criteria of(Long page, Long limit) {
        return new Criteria(validatePage(page), validateLimit(limit));
    }

    private static int validatePage(Long page) {
        if (page == null) {
            page = DEFAULT_PAGE;
        }
        if (page < MIN_PAGE) {
            page = MIN_PAGE;
        }
        page -= 1; // start from 0
        return page.intValue();
    }

    private static int validateLimit(Long limit) {
        if (limit == null) {
            limit = DEFAULT_LIMIT;
        }
        if (limit < MIN_LIMIT) {
            limit = MIN_LIMIT;
        }
        if (limit > MAX_LIMIT) {
            limit = MAX_LIMIT;
        }
        return limit.intValue();
    }
}
