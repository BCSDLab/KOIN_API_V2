package in.koreatech.koin.domain.community.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.community.exception.ArticleNotFoundException;
import in.koreatech.koin.domain.community.model.Board;

public interface BoardRepository extends Repository<Board, Integer> {
    Optional<Board> findById(Integer id);

    Board save(Board board);

    default Board getById(Integer boardId) {
        return findById(boardId).orElseThrow(
            () -> ArticleNotFoundException.withDetail("boardId: " + boardId));
    }
}
