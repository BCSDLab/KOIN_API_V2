package in.koreatech.koin.domain.dining.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.coop.exception.MenuNotFoundException;
import in.koreatech.koin.domain.dining.model.Dining;
import in.koreatech.koin.domain.dining.model.DiningType;
import in.koreatech.koin.config.repository.JpaRepository;

@JpaRepository
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

    List<Dining> findByDateBetween(LocalDate startDate, LocalDate endDate);

    List<Dining> findByDateBetweenAndPlaceIn(LocalDate startDate, LocalDate endDate, List<String> placeFilters);

    List<Dining> findByDateBetweenAndImageUrlIsNotNull(LocalDate startDate, LocalDate endDate);

    List<Dining> findByDateBetweenAndImageUrlIsNotNullAndPlaceIn(LocalDate startDate, LocalDate endDate,
        List<String> placeFilters);

    Optional<List<Dining>> findByDate(LocalDate now);

    default List<Dining> getByDate(LocalDate now) {
        return findByDate(now)
            .orElseThrow(() -> MenuNotFoundException.withDetail("menuId: " + now));
    }

    @Query(
        "SELECT COUNT(d) = (SELECT COUNT(d2) FROM Dining d2 WHERE d2.date = :date AND d2.type = :type AND d2.place IN :places) "
            +
            "FROM Dining d WHERE d.date = :date AND d.type = :type AND d.place IN :places AND d.imageUrl IS NOT NULL")
    boolean allExistsByDateAndTypeAndPlacesAndImageUrlIsNotNull(LocalDate date, DiningType type, List<String> places);
}
