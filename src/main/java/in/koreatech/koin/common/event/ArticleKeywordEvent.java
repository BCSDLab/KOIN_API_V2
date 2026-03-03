package in.koreatech.koin.common.event;

import java.util.List;

import in.koreatech.koin.domain.community.keyword.model.ArticleKeyword;

public record ArticleKeywordEvent(
    Integer articleId,
    Integer authorId,
    List<ArticleKeyword> matchedKeywords
) {

}
