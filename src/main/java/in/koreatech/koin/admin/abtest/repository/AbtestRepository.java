package in.koreatech.koin.admin.abtest.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;

import in.koreatech.koin.admin.abtest.exception.AbtestNotFoundException;
import in.koreatech.koin.admin.abtest.model.Abtest;

public interface AbtestRepository extends Repository<Abtest, Integer> {

    Optional<Abtest> findById(Integer id);

    Abtest save(Abtest build);

    default Abtest getById(Integer id) {
        return findById(id).orElseThrow(() ->
            AbtestNotFoundException.withDetail("AbtestId: " + id));
    }

    Optional<Abtest> findByTitle(String title);

    default Abtest getByTitle(String title) {
        return findByTitle(title).orElseThrow(() -> AbtestNotFoundException.withDetail("Abtest Title: " + title));
    }

    Long countBy();

    Page<Abtest> findAll(Pageable pageable);

    void deleteById(Integer abtestId);
}
