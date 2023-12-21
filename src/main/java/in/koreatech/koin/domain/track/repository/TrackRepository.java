package in.koreatech.koin.domain.track.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.track.domain.Track;

public interface TrackRepository extends Repository<Track, Long> {

    Track save(Track track);

    List<Track> findAll();

    Optional<Track> findById(Long id);
}
