package in.koreatech.koin.acceptance.fixture;

import java.time.LocalDate;
import java.util.ArrayList;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.community.article.model.Article;
import in.koreatech.koin.domain.community.article.model.ArticleAttachment;
import in.koreatech.koin.domain.community.article.model.Board;
import in.koreatech.koin.domain.community.article.model.KoinArticle;
import in.koreatech.koin.domain.community.article.model.KoreatechArticle;
import in.koreatech.koin.domain.community.article.repository.ArticleRepository;
import in.koreatech.koin.domain.user.model.User;

@Component
@SuppressWarnings("NonAsciiCharacters")
public class ArticleFixture {

    private final ArticleRepository articleRepository;

    public ArticleFixture(
        ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    public Article 자유글_1(Board board, User user) {

        KoinArticle koinArticle = KoinArticle.builder()
            .user(user)
            .isDeleted(false)
            .build();

        Article article = Article.builder()
            .board(board)
            .title("자유글 1의 제목입니다")
            .content("<p>내용</p>")
            .hit(1)
            .isDeleted(false)
            .attachments(new ArrayList<>())
            .koinArticle(koinArticle)
            .build();

        koinArticle.setArticle(article);

        article.getAttachments().add(
            ArticleAttachment.builder()
                .article(article)
                .url("https://example.com")
                .name("첨부파일1.png")
                .hash("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855")
                .build()
        );

        return articleRepository.save(article);
    }

    public Article 자유글_2(Board board, User user) {

        KoinArticle koinArticle = KoinArticle.builder()
            .user(user)
            .isDeleted(false)
            .build();

        Article article = Article.builder()
            .board(board)
            .title("자유 2글의 제목입니다")
            .content("<p>내용</p>")
            .hit(1)
            .isDeleted(false)
            .attachments(new ArrayList<>())
            .koinArticle(koinArticle)
            .build();

        koinArticle.setArticle(article);

        return articleRepository.save(article);
    }

    public Article 공지_크롤링_게시글(String title, Board board, Integer articleNum) {

        KoreatechArticle koreatechArticle = KoreatechArticle.builder()
            .url("https://example3.com")
            .portalNum(articleNum)
            .portalHit(1)
            .isDeleted(false)
            .author("취창업 지원팀")
            .registeredAt(LocalDate.of(2024, 10, 3))
            .build();

        Article article = Article.builder()
            .board(board)
            .title(title)
            .content("<p>내용</p>")
            .hit(1)
            .isDeleted(false)
            .attachments(new ArrayList<>())
            .koreatechArticle(koreatechArticle)
            .build();

        article.getAttachments().add(
            ArticleAttachment.builder()
                .article(article)
                .url("https://example.com")
                .name("첨부파일1.png")
                .hash("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855")
                .build()
        );

        koreatechArticle.setArticle(article);

        return articleRepository.save(article);
    }
}
