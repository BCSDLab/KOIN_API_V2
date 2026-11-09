package in.koreatech.koin.domain.translation.model;

public enum TranslationType {
    ARTICLE_SUMMARY, LOST_ITEM, RECRUITMENT, LOST_ITEM_MESSAGE, RECRUITMENT_MESSAGE;

    public boolean isMessage() {
        return this == LOST_ITEM_MESSAGE || this == RECRUITMENT_MESSAGE;
    }
}
