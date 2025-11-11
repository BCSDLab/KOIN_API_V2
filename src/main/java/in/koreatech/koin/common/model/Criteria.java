package in.koreatech.koin.common.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort.Direction;

@Getter
@RequiredArgsConstructor
public class Criteria {
    public static final Integer DEFAULT_PAGE = 1;
    private static final Integer MIN_PAGE = 1;

    public static final Integer DEFAULT_LIMIT = 10;
    private static final Integer MIN_LIMIT = 1;
    private static final Integer MAX_LIMIT = 50;

    private final int page;
    private final int limit;

    public static Criteria of(Integer page, Integer limit) {
        return new Criteria(validateAndCalculatePage(page), validateAndCalculateLimit(limit));
    }

    public static Criteria of(Integer page, Integer limit, Integer total) {
        int validatedLimit = validateAndCalculateLimit(limit);
        return new Criteria(validateAndCalculatePage(page, validatedLimit, total), validatedLimit);
    }

    @Getter
    public enum Sort {
        CREATED_AT_ASC(Direction.ASC),
        CREATED_AT_DESC(Direction.DESC);

        private final Direction direction;

        Sort(Direction direction) {
            this.direction = direction;
        }

        public Direction getDirection() {
            return direction;
        }

    }

    private static int validateAndCalculatePage(Integer page) {
        if (page == null) {
            page = DEFAULT_PAGE;
        }
        if (page < MIN_PAGE) {
            page = MIN_PAGE;
        }
        page -= 1; // start from 0
        return page;
    }

    private static int validateAndCalculatePage(Integer page, Integer limit, Integer total) {
        int totalPage = total.equals(0) ? 1 : (int)Math.ceil((double)total / limit);

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
        return page;
    }

    private static int validateAndCalculateLimit(Integer limit) {
        if (limit == null) {
            limit = DEFAULT_LIMIT;
        }
        if (limit < MIN_LIMIT) {
            limit = MIN_LIMIT;
        }
        if (limit > MAX_LIMIT) {
            limit = MAX_LIMIT;
        }
        return limit;
    }
}
