package in.koreatech.koin.domain.club.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.club.exception.ClubNotFoundException;
import in.koreatech.koin.domain.club.model.Club;
import in.koreatech.koin.domain.club.model.ClubCategory;
import jakarta.persistence.LockModeType;

public interface ClubRepository extends Repository<Club, Integer> {

    List<Club> findAll();

    Optional<Club> findById(Integer id);

    default Club getById(Integer id) {
        return findById(id).orElseThrow(() -> ClubNotFoundException.withDetail("id : " + id));
    }

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Club c WHERE c.id = :id")
    Club findByIdWithPessimisticLock(@Param("id") Integer id);

    default Club getByIdWithPessimisticLock(Integer id) {
        Club club = findByIdWithPessimisticLock(id);

        if (club == null) {
            throw ClubNotFoundException.withDetail("id : " + id);
        }

        return club;
    }

    Club save(Club club);

    List<Club> findByClubCategory(ClubCategory category);

    List<Club> findByClubCategoryOrderByIdAsc(ClubCategory category);

    List<Club> findByClubCategoryOrderByHitsDesc(ClubCategory category);

    List<Club> findByOrderByHitsDesc();

    List<Club> findByOrderByIdAsc();

    void deleteById(Integer id);
}
