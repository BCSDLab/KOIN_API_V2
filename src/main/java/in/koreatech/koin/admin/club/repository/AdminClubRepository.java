package in.koreatech.koin.admin.club.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.admin.club.exception.ClubNotFoundException;
import in.koreatech.koin.domain.club.club.model.Club;
import in.koreatech.koin.global.config.repository.JpaRepository;

@JpaRepository
public interface AdminClubRepository extends Repository<Club, Integer> {

    Integer countByClubCategoryId(Integer clubCategoryId);

    Integer count();

    @Query(value =
        """
        SELECT * FROM club
        WHERE club_category_id = :clubCategoryId
        """, nativeQuery = true)
    Page<Club> findAllByClubCategoryId(@Param("clubCategoryId") Integer clubCategoryId, Pageable pageable);

    @Query(value =
        """
        SELECT * FROM club
        """, nativeQuery = true)
    Page<Club> findAll(Pageable pageable);

    Optional<Club> findById(Integer clubId);

    default Club getById(Integer clubId) {
        return findById(clubId)
            .orElseThrow(() -> ClubNotFoundException.withDetail("clubId : " + clubId));
    }

    Club save(Club club);
}
