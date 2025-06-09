package in.koreatech.koin.acceptance.fixture;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.member.model.Track;
import in.koreatech.koin.domain.member.repository.TrackRepository;

@Component
public class TrackFixture {

    private final TrackRepository trackRepository;

    public TrackFixture(TrackRepository trackRepository) {
        this.trackRepository = trackRepository;
    }

    public Track backend() {
        return trackRepository.save(
            Track.builder()
                .name("BackEnd")
                .build()
        );
    }

    public Track frontend() {
        return trackRepository.save(
            Track.builder()
                .name("FrontEnd")
                .build()
        );
    }

    public Track ios() {
        return trackRepository.save(
            Track.builder()
                .name("iOS")
                .build()
        );
    }

    public Track ai() {
        return trackRepository.save(
            Track.builder()
                .name("AI")
                .isDeleted(true)
                .build()
        );
    }
}
