package in.koreatech.koin.domain.club.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.club.exception.ClubNotFoundException;
import in.koreatech.koin.domain.club.model.Club;
import in.koreatech.koin.domain.club.model.ClubCategory;

public interface ClubRepository extends Repository<Club, Integer> {

    List<Club> findAll();

    Optional<Club> findById(Integer id);

    default Club getById(Integer id) {
        return findById(id).orElseThrow(() -> ClubNotFoundException.withDetail("id : " + id));
    }

    @Modifying
    @Query("UPDATE Club c SET c.hits = c.hits + 1 WHERE c.id = :id")
    void incrementHits(Integer id);

    Club save(Club club);

    List<Club> findByClubCategory(ClubCategory category);

    List<Club> findByIsActiveTrueAndClubCategoryOrderByIdAsc(ClubCategory category);

    List<Club> findByIsActiveTrueAndClubCategoryOrderByHitsDesc(ClubCategory category);

    List<Club> findByIsActiveTrueOrderByHitsDesc();

    List<Club> findByIsActiveTrueOrderByIdAsc();

    void deleteById(Integer id);
}
