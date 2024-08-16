package in.koreatech.koin.domain.community.articles.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.community.articles.exception.ArticleNotFoundException;
import in.koreatech.koin.domain.community.articles.model.Board;

public interface BoardRepository extends Repository<Board, Integer> {
    Optional<Board> findById(Integer id);

    Board save(Board board);

    default Board getById(Integer boardId) {
        return findById(boardId).orElseThrow(
            () -> ArticleNotFoundException.withDetail("boardId: " + boardId));
    }

    Long countBy();
}
