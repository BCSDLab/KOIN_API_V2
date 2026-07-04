package in.koreatech.koin.unit.domain.community.keyword.service;

import static in.koreatech.koin.domain.community.keyword.enums.KeywordCategory.KOREATECH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import in.koreatech.koin.domain.community.keyword.model.ArticleKeyword;
import in.koreatech.koin.domain.community.keyword.repository.ArticleKeywordUserMapRepository;
import in.koreatech.koin.domain.community.keyword.service.ArticleKeywordUserMatcher;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.unit.fixture.KeywordFixture;
import in.koreatech.koin.unit.fixture.UserFixture;

@ExtendWith(MockitoExtension.class)
public class ArticleKeywordUserMatcherTest {

    @InjectMocks
    private ArticleKeywordUserMatcher articleKeywordUserMatcher;

    @Mock
    private ArticleKeywordUserMapRepository articleKeywordUserMapRepository;

    @Test
    void 매칭된_키워드로_사용자별_키워드를_조회한다() {
        List<String> matchedKeywords = List.of("수강신청", "장학금");
        when(articleKeywordUserMapRepository.findAllByArticleKeywordCategoryAndArticleKeywordKeywordIn(
            KOREATECH,
            matchedKeywords
        )).thenReturn(List.of());

        articleKeywordUserMatcher.findKeywordsByUserId(KOREATECH, matchedKeywords);

        verify(articleKeywordUserMapRepository)
            .findAllByArticleKeywordCategoryAndArticleKeywordKeywordIn(KOREATECH, matchedKeywords);
    }

    @Test
    void 키워드_사용자_매핑을_사용자별_키워드로_변환한다() {
        User firstUser = UserFixture.id_설정_코인_유저(1);
        User secondUser = UserFixture.id_설정_코인_유저(2);
        ArticleKeyword firstKeyword = KeywordFixture.공지_키워드("수강신청");
        ArticleKeyword secondKeyword = KeywordFixture.공지_키워드("장학금");
        List<String> matchedKeywords = List.of("수강신청", "장학금");
        when(articleKeywordUserMapRepository.findAllByArticleKeywordCategoryAndArticleKeywordKeywordIn(
            KOREATECH,
            matchedKeywords
        )).thenReturn(List.of(
            KeywordFixture.키워드_사용자_매핑(firstUser, firstKeyword),
            KeywordFixture.키워드_사용자_매핑(secondUser, secondKeyword)
        ));

        Map<Integer, String> keywordByUserId = articleKeywordUserMatcher.findKeywordsByUserId(
            KOREATECH,
            matchedKeywords
        );

        assertThat(keywordByUserId).containsExactlyInAnyOrderEntriesOf(Map.of(
            firstUser.getId(), firstKeyword.getKeyword(),
            secondUser.getId(), secondKeyword.getKeyword()
        ));
    }

    @Test
    void 한_사용자가_여러_키워드에_매칭되면_더_긴_키워드를_선택한다() {
        User user = UserFixture.id_설정_코인_유저(1);
        ArticleKeyword shortKeyword = KeywordFixture.공지_키워드("신청");
        ArticleKeyword longKeyword = KeywordFixture.공지_키워드("수강신청");
        List<String> matchedKeywords = List.of("신청", "수강신청");
        when(articleKeywordUserMapRepository.findAllByArticleKeywordCategoryAndArticleKeywordKeywordIn(
            KOREATECH,
            matchedKeywords
        )).thenReturn(List.of(
            KeywordFixture.키워드_사용자_매핑(user, shortKeyword),
            KeywordFixture.키워드_사용자_매핑(user, longKeyword)
        ));

        Map<Integer, String> keywordByUserId = articleKeywordUserMatcher.findKeywordsByUserId(
            KOREATECH,
            matchedKeywords
        );

        assertThat(keywordByUserId).containsExactly(Map.entry(user.getId(), longKeyword.getKeyword()));
    }

    @Test
    void 한_사용자가_같은_길이의_키워드에_매칭되면_먼저_조회된_키워드를_유지한다() {
        User user = UserFixture.id_설정_코인_유저(1);
        ArticleKeyword firstKeyword = KeywordFixture.공지_키워드("장학금");
        ArticleKeyword secondKeyword = KeywordFixture.공지_키워드("생활관");
        List<String> matchedKeywords = List.of("장학금", "생활관");
        when(articleKeywordUserMapRepository.findAllByArticleKeywordCategoryAndArticleKeywordKeywordIn(
            KOREATECH,
            matchedKeywords
        )).thenReturn(List.of(
            KeywordFixture.키워드_사용자_매핑(user, firstKeyword),
            KeywordFixture.키워드_사용자_매핑(user, secondKeyword)
        ));

        Map<Integer, String> keywordByUserId = articleKeywordUserMatcher.findKeywordsByUserId(
            KOREATECH,
            matchedKeywords
        );

        assertThat(keywordByUserId).containsExactly(Map.entry(user.getId(), firstKeyword.getKeyword()));
    }

}
