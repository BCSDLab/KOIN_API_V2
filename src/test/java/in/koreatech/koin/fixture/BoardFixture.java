package in.koreatech.koin.fixture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.community.articles.model.Board;
import in.koreatech.koin.domain.community.articles.repository.BoardRepository;

@Component
@SuppressWarnings("NonAsciiCharacters")
public class BoardFixture {

    @Autowired
    private final BoardRepository boardRepository;

    @Autowired
    public BoardFixture(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

    public Board 자유게시판() {
        return boardRepository.save(
            Board.builder()
                .tag("FA001")
                .name("자유게시판")
                .isAnonymous(false)
                .articleCount(0)
                .isDeleted(false)
                .isNotice(false)
                .parentId(null)
                .seq(1)
                .build()
        );
    }

    public BoardFixtureBuilder builder() {
        return new BoardFixtureBuilder();
    }

    public final class BoardFixtureBuilder {

        private String tag;
        private String name;
        private boolean isAnonymous;
        private Integer articleCount;
        private boolean isDeleted;
        private boolean isNotice;
        private Integer parentId;
        private Integer seq;

        public BoardFixtureBuilder tag(String tag) {
            this.tag = tag;
            return this;
        }

        public BoardFixtureBuilder name(String name) {
            this.name = name;
            return this;
        }

        public BoardFixtureBuilder isAnonymous(boolean isAnonymous) {
            this.isAnonymous = isAnonymous;
            return this;
        }

        public BoardFixtureBuilder articleCount(Integer articleCount) {
            this.articleCount = articleCount;
            return this;
        }

        public BoardFixtureBuilder isDeleted(boolean isDeleted) {
            this.isDeleted = isDeleted;
            return this;
        }

        public BoardFixtureBuilder isNotice(boolean isNotice) {
            this.isNotice = isNotice;
            return this;
        }

        public BoardFixtureBuilder parentId(Integer parentId) {
            this.parentId = parentId;
            return this;
        }

        public BoardFixtureBuilder seq(Integer seq) {
            this.seq = seq;
            return this;
        }

        public Board build() {
            return boardRepository.save(
                Board.builder()
                    .tag(tag)
                    .isDeleted(isDeleted)
                    .isAnonymous(isAnonymous)
                    .parentId(parentId)
                    .seq(seq)
                    .isNotice(isNotice)
                    .name(name)
                    .articleCount(articleCount)
                    .build()
            );
        }
    }
}
