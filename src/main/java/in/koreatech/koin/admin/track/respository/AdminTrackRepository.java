package in.koreatech.koin.admin.track.respository;

import java.util.List;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.member.model.Track;

public interface AdminTrackRepository extends Repository<Track, Integer> {

    Track save(Track track);

    List<Track> findAll();
}
