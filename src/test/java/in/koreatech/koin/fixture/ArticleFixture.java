package in.koreatech.koin.fixture;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.community.article.model.Article;
import in.koreatech.koin.domain.community.article.model.ArticleAttachment;
import in.koreatech.koin.domain.community.article.model.Board;
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

    public Article 자유글_1(Board board) {
        Article article = Article.builder()
            .board(board)
            .title("자유 글의 제목입니다")
            .content("<p>내용</p>")
            .author("작성자1")
            .hit(1)
            .koinHit(1)
            .isDeleted(false)
            .articleNum(1)
            .url("https://example.com")
            .attachments(new ArrayList<>())
            .registeredAt(LocalDate.of(2024, 1, 15))
            .build();

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

    public Article 자유글_2(Board board) {
        return articleRepository.save(
            Article.builder()
                .board(board)
                .title("자유 글2의 제목입니다")
                .content("<p>내용222</p>")
                .author("작성자2")
                .hit(1)
                .koinHit(1)
                .isDeleted(false)
                .articleNum(2)
                .url("https://example2.com")
                .attachments(List.of())
                .registeredAt(LocalDate.of(2024, 1, 15))
                .build()
        );
    }

    public Article 자유글_3(String title, Board board, Integer articleNum) {
        return articleRepository.save(
            Article.builder()
                .board(board)
                .title(title)
                .content("<p>내용333</p>")
                .author("작성자3")
                .hit(1)
                .koinHit(1)
                .isDeleted(false)
                .articleNum(articleNum)
                .url("https://example3.com")
                .attachments(List.of())
                .registeredAt(LocalDate.of(2024, 1, 15))
                .isNotice(false)
                .build()
        );
    }
}
