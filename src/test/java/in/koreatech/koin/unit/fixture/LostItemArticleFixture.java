package in.koreatech.koin.unit.fixture;

import java.time.LocalDate;

import org.springframework.test.util.ReflectionTestUtils;

import in.koreatech.koin.domain.community.article.model.Article;
import in.koreatech.koin.domain.community.article.model.Board;
import in.koreatech.koin.domain.community.article.model.LostItemArticle;
import in.koreatech.koin.domain.user.model.User;

public class LostItemArticleFixture {

    private LostItemArticleFixture() {}

    public static LostItemArticle 분실물_게시글_학생등록(Integer articleId, Integer lostItemArticleId, User author) {
        Board lostItemArticleBoard = Board.builder()
            .name("분실물게시판")
            .isAnonymous(false)
            .articleCount(0)
            .isDeleted(false)
            .isNotice(false)
            .parentId(null)
            .build();
        ReflectionTestUtils.setField(lostItemArticleBoard, "id", 14);

        Article article = Article.builder()
            .id(articleId)
            .board(lostItemArticleBoard)
            .title("신분증 | 학교 | 24.12.17")
            .content("학생회관 앞 계단에 …")
            .hit(0)
            .isNotice(false)
            .isDeleted(false)
            .build();

        LostItemArticle lostItemArticle = LostItemArticle.builder()
            .article(article)
            .author(author)
            .category("신분증")
            .foundDate(LocalDate.of(2024, 12, 17))
            .foundPlace("학교")
            .isDeleted(false)
            .type("LOST")
            .isCouncil(false)
            .build();

        ReflectionTestUtils.setField(lostItemArticle, "id", lostItemArticleId);
        ReflectionTestUtils.setField(article, "lostItemArticle", lostItemArticle);

        return lostItemArticle;
    }
}
