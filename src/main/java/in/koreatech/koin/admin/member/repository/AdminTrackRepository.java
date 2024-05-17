package in.koreatech.koin.admin.member.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.member.exception.TrackNotFoundException;
import in.koreatech.koin.domain.member.model.Track;

public interface AdminTrackRepository extends Repository<Track, Integer> {

    Track save(Track track);

    List<Track> findAll();

    Optional<Track> findByName(String name);

    default Track getByName(String name) {
        return findByName(name)
            .orElseThrow(() -> TrackNotFoundException.withDetail("trackName: " + name));
    }
}
