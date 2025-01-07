package in.koreatech.koin.domain.timetableV3.model;

import in.koreatech.koin.global.exception.KoinIllegalArgumentException;
import lombok.Getter;

@Getter
public enum Term {
    WINTER("겨울학기", 1),
    SECOND("2학기", 2),
    SUMMER("여름학기", 3),
    FIRST("1학기", 4);

    private final String description;
    private final int priority;

    Term(String description, int priority) {
        this.description = description;
        this.priority = priority;
    }

    public static Term fromDescription(String description) {
        for (Term term : Term.values()) {
            if (term.description.equals(description)) {
                return term;
            }
        }
        throw new KoinIllegalArgumentException("term 양식이 잘못됐습니다.");
    }
}
