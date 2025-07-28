package in.koreatech.koin.common.event;

import in.koreatech.koin.domain.community.keyword.model.ArticleKeyword;

public record ArticleKeywordEvent(
    Integer articleId,
    Integer authorId,
    ArticleKeyword keyword
) {

}
