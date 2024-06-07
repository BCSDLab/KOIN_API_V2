package in.koreatech.koin.domain.coop.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.coop.exception.CoopNotFoundException;
import in.koreatech.koin.domain.coop.model.Coop;

public interface CoopRepository extends Repository<Coop, Integer> {
    Optional<Coop> findByCoopId(String coopId);

    default Coop getByCoopId(String coopId){
        return findByCoopId(coopId).orElseThrow(() -> CoopNotFoundException.withDetail("CoopId : " + coopId));
    }

    Coop save(Coop coop);
}
