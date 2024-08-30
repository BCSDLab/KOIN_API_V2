package in.koreatech.koin.fixture;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.community.model.Article;
import in.koreatech.koin.domain.community.model.ArticleAttachment;
import in.koreatech.koin.domain.community.model.ArticleKeyword;
import in.koreatech.koin.domain.community.model.ArticleKeywordUserMap;
import in.koreatech.koin.domain.community.model.Board;
import in.koreatech.koin.domain.community.repository.ArticleKeywordRepository;
import in.koreatech.koin.domain.community.repository.ArticleKeywordUserMapRepository;
import in.koreatech.koin.domain.community.repository.ArticleRepository;
import in.koreatech.koin.domain.user.model.User;

@Component
@SuppressWarnings("NonAsciiCharacters")
public class ArticleFixture {

    private final ArticleRepository articleRepository;
    private final ArticleKeywordRepository articleKeywordRepository;
    private final ArticleKeywordUserMapRepository articleKeywordUserMapRepository;

    public ArticleFixture(
        ArticleRepository articleRepository,
        ArticleKeywordRepository articleKeywordRepository,
        ArticleKeywordUserMapRepository articleKeywordUserMapRepository) {
        this.articleRepository = articleRepository;
        this.articleKeywordRepository = articleKeywordRepository;
        this.articleKeywordUserMapRepository = articleKeywordUserMapRepository;
    }

    public Article 자유글_1(Board board) {
        Article article = Article.builder()
            .board(board)
            .title("자유 글의 제목입니다")
            .content("<p>내용</p>")
            .author("작성자1")
            .hit(1)
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
                .isDeleted(false)
                .articleNum(2)
                .url("https://example2.com")
                .attachments(List.of())
                .registeredAt(LocalDate.of(2024, 1, 15))
                .build()
        );
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
