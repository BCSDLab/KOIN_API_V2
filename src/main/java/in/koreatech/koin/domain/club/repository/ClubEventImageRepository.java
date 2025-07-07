package in.koreatech.koin.domain.club.repository;

import java.util.List;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.club.model.ClubEvent;
import in.koreatech.koin.domain.club.model.ClubEventImage;

public interface ClubEventImageRepository extends Repository<ClubEventImage, Integer> {

    void save(ClubEventImage clubEventImages);

    void deleteAllByClubEvent(ClubEvent clubEvent);
}
