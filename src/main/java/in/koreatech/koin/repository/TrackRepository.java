package in.koreatech.koin.repository;

import in.koreatech.koin.domain.Track;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;

public interface TrackRepository extends Repository<Track, Long> {

    Track save(Track track);

    List<Track> findAll();

    Optional<Track> findById(Long id);
}
