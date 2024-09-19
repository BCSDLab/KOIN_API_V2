package in.koreatech.koin.domain.dining.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.coop.exception.MenuNotFoundException;
import in.koreatech.koin.domain.dining.model.Dining;
import in.koreatech.koin.domain.dining.model.DiningType;

public interface DiningRepository extends Repository<Dining, Integer> {

    Dining save(Dining dining);

    Optional<Dining> findById(Integer id);

    List<Dining> findAllByDate(LocalDate date);

    default Dining getById(Integer id) {
        return findById(id)
            .orElseThrow(() -> MenuNotFoundException.withDetail("menuId: " + id));
    }

    List<Dining> findAllByDateAndType(LocalDate date, DiningType type);

    boolean existsByDateAndTypeAndImageUrlIsNotNull(LocalDate date, DiningType type);

    Long count();

    Page<Dining> findAllByMenuContainingAndPlaceIn(String keyword, List<String> diningPlaces, Pageable pageable);
}
