package in.koreatech.koin.admin.club.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.admin.club.exception.ClubNotFound;
import in.koreatech.koin.domain.club.model.Club;
import in.koreatech.koin.domain.club.model.ClubAdmin;

public interface AdminClubRepository extends Repository<Club, Integer> {

    Integer countByClubCategoryId(Integer clubCategoryId);

    Integer count();

    @Query(value = """
        SELECT * FROM club
        WHERE club_category_id = :clubCategoryId
        """, nativeQuery = true)
    Page<Club> findAllByClubCategoryId(@Param("clubCategoryId") Integer clubCategoryId, Pageable pageable);

    @Query(value = """
        SELECT * FROM club
        """, nativeQuery = true)
    Page<Club> findAll(Pageable pageable);

    @Query("""
        SELECT ca FROM ClubAdmin ca
        JOIN FETCH ca.user u
        JOIN FETCH ca.club c
        WHERE ca.isAccept = true
        """)
    Page<ClubAdmin> findAcceptedPageAll(Pageable pageable);

    @Query("""
        SELECT COUNT(ca) FROM ClubAdmin ca
        JOIN ca.user u
        WHERE ca.isAccept = true
        """)
    int countAcceptedAll();

    @Query("""
        SELECT ca FROM ClubAdmin ca
        JOIN FETCH ca.user u
        JOIN FETCH ca.club c
        WHERE ca.isAccept = false
        """)
    Page<ClubAdmin> findUnacceptedPageAll(Pageable pageable);

    @Query("""
        SELECT COUNT(ca) FROM ClubAdmin ca
        JOIN ca.user u
        WHERE ca.isAccept = false
        """)
    int countUnacceptedAll();

    Optional<Club> findById(Integer clubId);

    default Club getById(Integer clubId) {
        return findById(clubId)
            .orElseThrow(() -> ClubNotFound.withDetail("clubId : " + clubId));
    }

    Club save(Club club);
}
