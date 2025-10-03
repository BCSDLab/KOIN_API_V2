package in.koreatech.koin.admin.member.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.admin.member.exception.TechStackNotFoundException;
import in.koreatech.koin.domain.member.model.TechStack;
import in.koreatech.koin.global.config.repository.JpaRepository;

@JpaRepository
public interface AdminTechStackRepository extends Repository<TechStack, Integer> {

    TechStack save(TechStack techStack);

    Optional<TechStack> findById(Integer id);

    List<TechStack> findAllByTrackId(Integer id);

    default TechStack getById(Integer id) {
        return findById(id)
            .orElseThrow(() -> TechStackNotFoundException.withDetail("id : " + id));
    }
}
