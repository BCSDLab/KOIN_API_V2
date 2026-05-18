package in.koreatech.koin.domain.community.article.service.summary;

import java.util.Arrays;

public enum ArticleSummaryIcon {
    CALENDAR("📅"),
    TARGET("🎯"),
    LOCATION("📍"),
    ACTION("📝"),
    MONEY("💰"),
    NOTICE("📌"),
    DOCUMENT("📎"),
    DEFAULT("✅");

    private final String emoji;

    ArticleSummaryIcon(String emoji) {
        this.emoji = emoji;
    }

    public String getEmoji() {
        return emoji;
    }

    public static ArticleSummaryIcon from(String iconKey) {
        if (iconKey == null || iconKey.isBlank()) {
            return DEFAULT;
        }
        return Arrays.stream(values())
            .filter(icon -> icon.name().equals(iconKey.trim().toUpperCase()))
            .findFirst()
            .orElse(DEFAULT);
    }
}
