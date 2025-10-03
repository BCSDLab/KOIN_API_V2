package in.koreatech.koin.domain.club.like.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.club.club.exception.ClubNotFoundException;
import in.koreatech.koin.domain.club.club.model.Club;
import in.koreatech.koin.domain.club.like.model.ClubLike;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.global.config.repository.JpaRepository;

@JpaRepository
public interface ClubLikeRepository extends Repository<ClubLike, Integer> {

    boolean existsByClubAndUser(Club club, User user);

    boolean existsByClubIdAndUserId(Integer clubId, Integer userId);

    Optional<ClubLike> findByClubAndUser(Club club, User user);

    default ClubLike getByClubAndUser(Club club, User user) {
        return findByClubAndUser(club, user)
            .orElseThrow(() -> ClubNotFoundException.withDetail("id : "));
    }

    void save(ClubLike clubLike);

    void deleteByClubAndUser(Club club, User user);

    @Query("SELECT cl.club.id FROM ClubLike cl WHERE cl.user.id = :userId")
    List<Integer> findClubIdsByUserId(@Param("userId") Integer userId);
}
