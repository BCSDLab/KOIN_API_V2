package in.koreatech.koin.domain.club.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.club.exception.ClubNotFoundException;
import in.koreatech.koin.domain.club.model.Club;

public interface ClubRepository extends Repository<Club, Integer> {

    List<Club> findAll();

    Optional<Club> findById(Integer id);

    default Club getById(Integer id) {
        return findById(id).orElseThrow(() -> ClubNotFoundException.withDetail("id : " + id));
    }

    void deleteById(Integer id);
}
