package in.koreatech.koin.domain.member.repository;

import java.util.List;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.member.model.TechStack;
import in.koreatech.koin.global.marker.JpaRepositoryMarker;

@JpaRepositoryMarker
public interface TechStackRepository extends Repository<TechStack, Integer> {

    TechStack save(TechStack techStack);

    List<TechStack> findAllByTrackId(Integer id);
}
