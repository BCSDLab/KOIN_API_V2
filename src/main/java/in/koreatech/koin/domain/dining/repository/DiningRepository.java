package in.koreatech.koin.domain.dining.repository;

import java.util.List;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.dining.model.Dining;

public interface DiningRepository extends Repository<Dining, Long> {

    Dining save(Dining dining);

    List<Dining> findAllByDate(String date);
}
