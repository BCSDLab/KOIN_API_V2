package in.koreatech.koin.domain.community.article.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.community.article.exception.BoardNotFoundException;
import in.koreatech.koin.domain.community.article.model.Board;
import in.koreatech.koin.global.marker.JpaRepositoryMarker;

@JpaRepositoryMarker
public interface BoardRepository extends Repository<Board, Integer> {
    Optional<Board> findById(Integer id);

    Board save(Board board);

    default Board getById(Integer boardId) {
        return findById(boardId).orElseThrow(
            () -> BoardNotFoundException.withDetail("boardId: " + boardId));
    }
}
