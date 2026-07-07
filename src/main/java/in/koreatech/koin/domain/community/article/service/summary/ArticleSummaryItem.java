package in.koreatech.koin.domain.community.article.service.summary;

public record ArticleSummaryItem(
    ArticleSummaryIcon icon,
    String text
) {

    public String formattedLine() {
        return icon.getEmoji() + " " + text;
    }
}
