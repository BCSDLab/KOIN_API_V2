package in.koreatech.koin.domain.club.event.repository;

import in.koreatech.koin.domain.club.event.model.ClubEvent;
import in.koreatech.koin.domain.club.event.model.ClubEventImage;
import org.springframework.data.repository.Repository;

public interface ClubEventImageRepository extends Repository<ClubEventImage, Integer> {

    void save(ClubEventImage clubEventImages);

    void deleteAllByClubEvent(ClubEvent clubEvent);
}
