package in.koreatech.koin.admin.member.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.member.exception.TrackNotFoundException;
import in.koreatech.koin.domain.member.model.Track;
import in.koreatech.koin.config.repository.JpaRepository;

@JpaRepository
public interface AdminTrackRepository extends Repository<Track, Integer> {

    Track save(Track track);

    List<Track> findAll();

    Optional<Track> findById(Integer trackId);

    Optional<Track> findByName(String trackName);

    default Track getById(Integer trackId) {
        return findById(trackId)
            .orElseThrow(() -> TrackNotFoundException.withDetail("trackId: " + trackId));
    }

    default Track getByName(String trackName) {
        return findByName(trackName)
            .orElseThrow(() -> TrackNotFoundException.withDetail("name: " + trackName));
    }
}
