package in.koreatech.koin.domain.track.repository;

import java.util.List;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.track.model.TechStack;

public interface TechStackRepository extends Repository<TechStack, Long> {

    TechStack save(TechStack techStack);

    List<TechStack> findAllByTrackId(Long id);
}
