package in.koreatech.koin.domain.club.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.club.exception.ClubNotFoundException;
import in.koreatech.koin.domain.club.model.Club;
import in.koreatech.koin.domain.club.model.ClubLike;
import in.koreatech.koin.domain.user.model.User;

public interface ClubLikeRepository extends Repository<ClubLike, Integer> {

    boolean existsByClubAndUser(Club club, User user);

    Optional<ClubLike> findByClubAndUser(Club club, User user);

    default ClubLike getByClubAndUser(Club club, User user) {
        return findByClubAndUser(club, user)
            .orElseThrow(() -> ClubNotFoundException.withDetail("id : "));
    }

    void save(ClubLike clubLike);

    void deleteByClubAndUser(Club club, User user);
}
