package in.koreatech.koin.domain.community.article.service.summary;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import in.koreatech.koin.domain.community.article.model.Article;

public record ArticleSummarySourceSeed(
    Integer articleId,
    String title,
    String content,
    String author,
    LocalDate registeredAt,
    LocalDateTime updatedAt,
    List<ArticleAttachmentSeed> attachments
) {

    public static ArticleSummarySourceSeed from(Article article, String content) {
        return new ArticleSummarySourceSeed(
            article.getId(),
            article.getTitle(),
            content,
            article.getAuthor(),
            article.getRegisteredAt(),
            article.getUpdatedAt(),
            article.getAttachments()
                .stream()
                .map(ArticleAttachmentSeed::from)
                .sorted(Comparator.comparing(ArticleAttachmentSeed::id))
                .toList()
        );
    }
}
