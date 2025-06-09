package in.koreatech.koin.acceptance.fixture;

import java.util.ArrayList;
import java.util.List;

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

    public List<Board> 코인_게시판_리스트() {
        List<Board> boards = new ArrayList<>();
        boards.add(자유게시판());
        boards.add(취업게시판());
        boards.add(익명게시판());
        boards.add(공지사항());
        boards.add(일반공지());
        boards.add(장학공지());
        boards.add(학사공지());
        boards.add(취업공지());
        boards.add(코인공지());
        return boards;
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
}
