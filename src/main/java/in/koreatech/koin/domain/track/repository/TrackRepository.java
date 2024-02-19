package in.koreatech.koin.domain.track.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.track.exception.TrackNotFoundException;
import in.koreatech.koin.domain.track.model.Track;

public interface TrackRepository extends Repository<Track, Long> {

    Track save(Track track);

    List<Track> findAll();

    Optional<Track> findById(Long trackId);

    default Track getById(Long trackId) {
        return findById(trackId).orElseThrow(() -> TrackNotFoundException.withDetail("trackId: " + trackId));
    }
}
