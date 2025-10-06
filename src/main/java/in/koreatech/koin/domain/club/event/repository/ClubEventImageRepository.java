package in.koreatech.koin.domain.club.event.repository;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.club.event.model.ClubEvent;
import in.koreatech.koin.domain.club.event.model.ClubEventImage;
import in.koreatech.koin.config.repository.JpaRepositoryMarker;

@JpaRepositoryMarker
public interface ClubEventImageRepository extends Repository<ClubEventImage, Integer> {

    void save(ClubEventImage clubEventImages);

    void deleteAllByClubEvent(ClubEvent clubEvent);
}
