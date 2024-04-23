package in.koreatech.koin.fixture;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.community.model.Article;
import in.koreatech.koin.domain.community.model.Board;
import in.koreatech.koin.domain.community.repository.ArticleRepository;
import in.koreatech.koin.domain.user.model.User;

@Component
@SuppressWarnings("NonAsciiCharacters")
public class ArticleFixture {

    private final ArticleRepository articleRepository;

    public ArticleFixture(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    public Article 자유글_1(User user, Board board) {
        return articleRepository.save(
            Article.builder()
                .board(board)
                .title("자유 글의 제목입니다")
                .content("<p>내용</p>")
                .user(user)
                .nickname(user.getNickname())
                .hit(1)
                .ip("123.21.234.321")
                .isSolved(false)
                .isDeleted(false)
                .commentCount((byte)0)
                .meta(null)
                .isNotice(false)
                .noticeArticleId(null)
                .build()
        );
    }

    public Article 자유글_2(User user, Board board) {
        return articleRepository.save(
            Article.builder()
                .board(board)
                .title("자유 글2의 제목입니다")
                .content("<p>내용222</p>")
                .user(user)
                .nickname(user.getNickname())
                .hit(1)
                .ip("127.0.0.1")
                .isSolved(false)
                .isDeleted(false)
                .commentCount((byte)0)
                .meta(null)
                .isNotice(false)
                .noticeArticleId(null)
                .build()
        );
    }

    public ArticleFixtureBuilder builder() {
        return new ArticleFixtureBuilder();
    }

    public final class ArticleFixtureBuilder {

        private Board board;
        private String title;
        private String content;
        private User user;
        private String nickname;
        private Integer hit;
        private String ip;
        private boolean isSolved;
        private boolean isDeleted;
        private Byte commentCount;
        private String meta;
        private boolean isNotice;
        private Integer noticeArticleId;

        public ArticleFixtureBuilder board(Board board) {
            this.board = board;
            return this;
        }

        public ArticleFixtureBuilder title(String title) {
            this.title = title;
            return this;
        }

        public ArticleFixtureBuilder content(String content) {
            this.content = content;
            return this;
        }

        public ArticleFixtureBuilder user(User user) {
            this.user = user;
            return this;
        }

        public ArticleFixtureBuilder nickname(String nickname) {
            this.nickname = nickname;
            return this;
        }

        public ArticleFixtureBuilder hit(Integer hit) {
            this.hit = hit;
            return this;
        }

        public ArticleFixtureBuilder ip(String ip) {
            this.ip = ip;
            return this;
        }

        public ArticleFixtureBuilder isSolved(boolean isSolved) {
            this.isSolved = isSolved;
            return this;
        }

        public ArticleFixtureBuilder isDeleted(boolean isDeleted) {
            this.isDeleted = isDeleted;
            return this;
        }

        public ArticleFixtureBuilder commentCount(Byte commentCount) {
            this.commentCount = commentCount;
            return this;
        }

        public ArticleFixtureBuilder meta(String meta) {
            this.meta = meta;
            return this;
        }

        public ArticleFixtureBuilder isNotice(boolean isNotice) {
            this.isNotice = isNotice;
            return this;
        }

        public ArticleFixtureBuilder noticeArticleId(Integer noticeArticleId) {
            this.noticeArticleId = noticeArticleId;
            return this;
        }

        public Article build() {
            return articleRepository.save(
                Article.builder()
                    .commentCount(commentCount)
                    .ip(ip)
                    .title(title)
                    .meta(meta)
                    .isSolved(isSolved)
                    .noticeArticleId(noticeArticleId)
                    .content(content)
                    .board(board)
                    .user(user)
                    .nickname(nickname)
                    .isNotice(isNotice)
                    .hit(hit)
                    .isDeleted(isDeleted)
                    .build()
            );
        }
    }
}
