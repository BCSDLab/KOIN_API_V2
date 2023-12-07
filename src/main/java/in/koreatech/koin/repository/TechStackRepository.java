package in.koreatech.koin.repository;

import in.koreatech.koin.domain.TechStack;
import java.util.List;
import org.springframework.data.repository.Repository;

public interface TechStackRepository extends Repository<TechStack, Long> {

    TechStack save(TechStack techStack);

    List<TechStack> findAllByTrackId(Long id);
}
