package in.koreatech.koin.domain.community.article.model.filter;

public enum LostItemFoundStatus {

    ALL,
    FOUND,
    NOT_FOUND;

    public Boolean getQueryStatus() {
        return switch (this) {
            case FOUND -> true;
            case NOT_FOUND -> false;
            case ALL -> null;
        };
    }
}
