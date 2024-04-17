package in.koreatech.koin.global.model;

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

    public static Criteria of(Long page, Long limit, Long total) {
        return new Criteria(validatePage(page, total, limit), validateLimit(limit));
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

    private static int validatePage(Long page, Long total, Long limit) {
        long totalPage = total.equals(0L) ? 1L : (int) Math.ceil((double) total / limit);

        if (page == null) {
            page = DEFAULT_PAGE;
        }
        if (page < MIN_PAGE) {
            page = MIN_PAGE;
        }
        if (page > totalPage) {
            page = totalPage;
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
