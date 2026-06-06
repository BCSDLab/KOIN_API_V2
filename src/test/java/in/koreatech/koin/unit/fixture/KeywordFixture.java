package in.koreatech.koin.unit.fixture;

import java.util.Map;

import in.koreatech.koin.common.event.KoreatechArticleKeywordEvent;
import in.koreatech.koin.domain.community.keyword.enums.KeywordCategory;
import in.koreatech.koin.domain.community.keyword.model.ArticleKeyword;
import in.koreatech.koin.domain.community.keyword.model.ArticleKeywordUserMap;
import in.koreatech.koin.domain.user.model.User;

public final class KeywordFixture {

    private KeywordFixture() {}

    public static ArticleKeyword 공지_키워드(String keyword) {
        return ArticleKeyword.builder()
            .keyword(keyword)
            .category(KeywordCategory.KOREATECH)
            .build();
    }

    public static ArticleKeywordUserMap 키워드_사용자_매핑(User user, ArticleKeyword articleKeyword) {
        return ArticleKeywordUserMap.builder()
            .user(user)
            .articleKeyword(articleKeyword)
            .build();
    }

    public static KoreatechArticleKeywordEvent 공지_키워드_이벤트(
        Integer articleId,
        Integer boardId,
        String articleTitle,
        Map<Integer, String> keywordByUserId
    ) {
        return KoreatechArticleKeywordEvent.of(
            articleId,
            boardId,
            articleTitle,
            keywordByUserId
        );
    }
}
