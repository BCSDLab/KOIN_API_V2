package in.koreatech.koin.domain.club.club.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.club.club.exception.ClubNotFoundException;
import in.koreatech.koin.domain.club.club.model.Club;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.config.repository.JpaRepository;
import in.koreatech.koin.global.exception.CustomException;
import jakarta.persistence.LockModeType;

@JpaRepository
public interface ClubRepository extends Repository<Club, Integer> {

    List<Club> findAll();

    Optional<Club> findById(Integer id);

    default Club getById(Integer id) {
        return findById(id)
            .orElseThrow(() -> CustomException.of(ApiResponseCode.NOT_FOUND_CLUB));
    }

    @Query("SELECT c.id FROM Club c WHERE c.id IN :ids")
    List<Integer> findExistingIds(List<Integer> ids);

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

    @Query("""
        SELECT c FROM Club c
        WHERE c.isActive = true AND c.normalizedName LIKE CONCAT(:query, '%') ORDER BY c.normalizedName
        """)
    List<Club> findByNamePrefix(String query, Pageable pageable);

    @Modifying
    @Query("UPDATE Club c SET c.hits = c.hits + :value WHERE c.id = :id")
    void incrementHitsByValue(Integer id, Integer value);

    Club save(Club club);

    void deleteById(Integer id);
}
