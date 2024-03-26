package in.koreatech.koin.domain.dining.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.coop.exception.MenuNotFoundException;
import in.koreatech.koin.domain.dining.model.Dining;

public interface DiningRepository extends Repository<Dining, Long> {

    Dining save(Dining dining);

    List<Dining> findAllByDate(String date);

    Optional<Dining> findById(Long id);

    default Dining getById(Long id){
        return findById(id)
            .orElseThrow(() -> MenuNotFoundException.withDetail("menuId: " + id));
    }
}
