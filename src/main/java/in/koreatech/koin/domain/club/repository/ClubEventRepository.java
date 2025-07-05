package in.koreatech.koin.domain.club.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin._common.code.ApiResponseCode;
import in.koreatech.koin._common.exception.CustomException;
import in.koreatech.koin.domain.club.exception.ClubEventNotFoundException;
import in.koreatech.koin.domain.club.model.ClubEvent;

public interface ClubEventRepository extends Repository<ClubEvent, Long> {

    void save(ClubEvent clubEvent);

    Optional<ClubEvent> findClubEventById(Integer id);

    Optional<ClubEvent> findByIdAndClubId(Integer id, Integer clubId);

    Optional<List<ClubEvent>> findAllByClubId(Integer clubId);

    default ClubEvent getClubEventById(Integer id) {
        return findClubEventById(id).orElseThrow(
            () -> CustomException.of(ApiResponseCode.NOT_FOUND_CLUB_EVENT));
    }

    default ClubEvent getClubEventByIdAndClubId(Integer id, Integer clubId) {
        return findByIdAndClubId(id, clubId)
            .orElseThrow(() -> CustomException.of(ApiResponseCode.NOT_FOUND_CLUB_EVENT));
    }

    default List<ClubEvent> getAllByClubId(Integer clubId) {
        return findAllByClubId(clubId).orElseThrow(
            () -> CustomException.of(ApiResponseCode.NOT_FOUND_CLUB_EVENT));
    }

    void delete(ClubEvent clubEvent);
}
