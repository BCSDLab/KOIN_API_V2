package in.koreatech.koin.domain.community.article.dto;

public record ArticleKeywordResult(
    Integer hotKeywordId,
    String keyword,
    Long count
) {

}
