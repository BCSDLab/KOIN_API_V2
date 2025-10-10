package in.koreatech.koin.domain.member.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.member.exception.TrackNotFoundException;
import in.koreatech.koin.domain.member.model.Track;
import in.koreatech.koin.global.marker.JpaRepositoryMarker;

@JpaRepositoryMarker
public interface TrackRepository extends Repository<Track, Integer> {

    Track save(Track track);

    List<Track> findAll();

    Optional<Track> findById(Integer trackId);

    default Track getById(Integer trackId) {
        return findById(trackId)
            .orElseThrow(() -> TrackNotFoundException.withDetail("trackId: " + trackId));
    }
}
