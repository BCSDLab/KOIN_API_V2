package in.koreatech.koin.domain.community.article.dto;

public record ArticleSummary(
    Integer id,
    String title
) {
    public static ArticleSummary of(Integer id, String title) {
        return new ArticleSummary(id, title);
    }
}
