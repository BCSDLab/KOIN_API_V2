package in.koreatech.koin.domain.dining.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.dining.model.Dining;

public interface DiningRepository extends Repository<Dining, Long> {

    Dining save(Dining dining);

    Dining findById(Long id);

    List<Dining> findAllByDate(String date);
}
