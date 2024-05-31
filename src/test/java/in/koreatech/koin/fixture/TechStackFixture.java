package in.koreatech.koin.fixture;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.member.model.TechStack;
import in.koreatech.koin.domain.member.model.Track;
import in.koreatech.koin.domain.member.repository.TechStackRepository;

@Component
public class TechStackFixture {

    private final TechStackRepository techStackRepository;

    public TechStackFixture(TechStackRepository techStackRepository) {
        this.techStackRepository = techStackRepository;
    }

    public TechStack java(Track track) {
        return techStackRepository.save(
            TechStack.builder()
                .imageUrl("https://testimageurl.com")
                .trackId(track.getId())
                .name("Java")
                .description("Language")
                .isDeleted(false)
                .build()
        );
    }
}
