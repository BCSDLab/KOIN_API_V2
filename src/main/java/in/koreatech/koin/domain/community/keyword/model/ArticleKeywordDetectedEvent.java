package in.koreatech.koin.domain.community.keyword.model;

public record ArticleKeywordDetectedEvent (
    Integer articleId,
    ArticleKeyword keyword
) {

}
