package in.koreatech.koin.domain.community.keyword.model;

public record ArticleKeywordEvent(
    Integer articleId,
    ArticleKeyword keyword
) {

}
