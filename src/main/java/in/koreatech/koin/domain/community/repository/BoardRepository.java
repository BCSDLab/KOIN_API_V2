package in.koreatech.koin.domain.community.repository;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.community.model.Board;

public interface BoardRepository extends Repository<Board, Long> {
    Board findById(Long id);
}
