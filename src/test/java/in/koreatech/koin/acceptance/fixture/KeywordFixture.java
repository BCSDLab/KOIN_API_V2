package in.koreatech.koin.acceptance.fixture;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.community.keyword.model.ArticleKeyword;
import in.koreatech.koin.domain.community.keyword.model.ArticleKeywordUserMap;
import in.koreatech.koin.domain.community.keyword.repository.ArticleKeywordRepository;
import in.koreatech.koin.domain.community.keyword.repository.ArticleKeywordUserMapRepository;
import in.koreatech.koin.domain.user.model.User;

@Component
@SuppressWarnings("NonAsciiCharacters")
public class KeywordFixture {

    private final ArticleKeywordRepository articleKeywordRepository;
    private final ArticleKeywordUserMapRepository articleKeywordUserMapRepository;

    public KeywordFixture(ArticleKeywordRepository articleKeywordRepository,
        ArticleKeywordUserMapRepository articleKeywordUserMapRepository) {
        this.articleKeywordRepository = articleKeywordRepository;
        this.articleKeywordUserMapRepository = articleKeywordUserMapRepository;
    }

    public ArticleKeywordUserMap 키워드1(String keyword, User user) {
        return 키워드1(keyword, user, false);
    }

    public ArticleKeywordUserMap 키워드1(String keyword, User user, boolean isFiltered) {
        ArticleKeyword articleKeyword = articleKeywordRepository.save(ArticleKeyword.builder()
            .keyword(keyword)
            .lastUsedAt(LocalDateTime.now())
            .isFiltered(isFiltered)
            .build());

        return articleKeywordUserMapRepository.save(
            ArticleKeywordUserMap.builder()
                .articleKeyword(articleKeyword)
                .user(user)
                .build()
        );
    }
}
