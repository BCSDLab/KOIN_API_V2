package in.koreatech.koin.fixture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.community.article.model.Board;
import in.koreatech.koin.domain.community.article.repository.BoardRepository;

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
                .name("자유게시판")
                .isAnonymous(false)
                .articleCount(0)
                .isDeleted(false)
                .isNotice(false)
                .build()
        );
    }

    public Board 취업게시판() {
        return boardRepository.save(
            Board.builder()
                .name("취업게시판")
                .isAnonymous(false)
                .articleCount(0)
                .isDeleted(false)
                .isNotice(false)
                .build()
        );
    }

    public Board 익명게시판() {
        return boardRepository.save(
            Board.builder()
                .name("자유게시판")
                .isAnonymous(true)
                .articleCount(0)
                .isDeleted(false)
                .isNotice(false)
                .build()
        );
    }

    public Board 공지사항() {
        return boardRepository.save(
            Board.builder()
                .name("공지사항")
                .isAnonymous(false)
                .articleCount(0)
                .isDeleted(false)
                .isNotice(true)
                .build()
        );
    }

    public Board 일반공지() {
        return boardRepository.save(
            Board.builder()
                .name("일반공지")
                .isAnonymous(false)
                .articleCount(0)
                .isDeleted(false)
                .isNotice(true)
                .build()
        );
    }

    public Board 장학공지() {
        return boardRepository.save(
            Board.builder()
                .name("장학공지")
                .isAnonymous(false)
                .articleCount(0)
                .isDeleted(false)
                .isNotice(true)
                .build()
        );
    }

    public Board 학사공지() {
        return boardRepository.save(
            Board.builder()
                .name("장학공지")
                .isAnonymous(false)
                .articleCount(0)
                .isDeleted(false)
                .isNotice(true)
                .build()
        );
    }

    public Board 취업공지() {
        return boardRepository.save(
            Board.builder()
                .name("장학공지")
                .isAnonymous(false)
                .articleCount(0)
                .isDeleted(false)
                .isNotice(true)
                .build()
        );
    }

    public Board 코인공지() {
        return boardRepository.save(
            Board.builder()
                .name("코인공지")
                .isAnonymous(false)
                .articleCount(0)
                .isDeleted(false)
                .isNotice(true)
                .build()
        );
    }

    public BoardFixtureBuilder builder() {
        return new BoardFixtureBuilder();
    }

    public final class BoardFixtureBuilder {

        private String name;
        private boolean isAnonymous;
        private Integer articleCount;
        private boolean isDeleted;
        private boolean isNotice;
        private Integer parentId;

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

        public Board build() {
            return boardRepository.save(
                Board.builder()
                    .isDeleted(isDeleted)
                    .isAnonymous(isAnonymous)
                    .parentId(parentId)
                    .isNotice(isNotice)
                    .name(name)
                    .articleCount(articleCount)
                    .build()
            );
        }
    }
}
