package in.koreatech.koin.fixture;

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

    public KeywordFixture(
        ArticleKeywordRepository articleKeywordRepository,
        ArticleKeywordUserMapRepository articleKeywordUserMapRepository) {
        this.articleKeywordRepository = articleKeywordRepository;
        this.articleKeywordUserMapRepository = articleKeywordUserMapRepository;
    }

    public ArticleKeywordUserMap 키워드1(String keyword, User user) {
        ArticleKeyword articleKeyword = articleKeywordRepository.save(ArticleKeyword.builder()
            .keyword(keyword)
            .lastUsedAt(LocalDateTime.now())
            .build());

        ArticleKeywordUserMap articleKeywordUserMap = ArticleKeywordUserMap.builder()
            .articleKeyword(articleKeyword)
            .user(user)
            .build();

        return articleKeywordUserMapRepository.save(articleKeywordUserMap);
    }
}
