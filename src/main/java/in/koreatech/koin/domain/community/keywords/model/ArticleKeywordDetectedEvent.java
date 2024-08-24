package in.koreatech.koin.domain.community.keywords.model;

public record ArticleKeywordDetectedEvent (
    Integer articleId,
    ArticleKeyword keyword
) {

}
