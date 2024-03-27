package in.koreatech.koin.domain.dining.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.coop.exception.MenuNotFoundException;
import in.koreatech.koin.domain.dining.model.Dining;
import in.koreatech.koin.domain.user.exception.UserNotFoundException;

public interface DiningRepository extends Repository<Dining, Long> {

    Dining save(Dining dining);

    Optional<Dining> findById(Long id);

    List<Dining> findAllByDate(String date);

    default Dining getById(Long id){
        return findById(id)
            .orElseThrow(() -> MenuNotFoundException.withDetail("menuId: " + id));
    }
}
