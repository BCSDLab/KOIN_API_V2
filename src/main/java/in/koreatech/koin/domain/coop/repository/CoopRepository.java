package in.koreatech.koin.domain.coop.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.coop.exception.CoopNotFoundException;
import in.koreatech.koin.domain.coop.model.Coop;

public interface CoopRepository extends Repository<Coop, Integer> {
    Optional<Coop> findByLoginId(String loginId);

    default Coop getByLoginId(String loginId){
        return findByLoginId(loginId).orElseThrow(() -> CoopNotFoundException.withDetail("loginId : " + loginId));
    }

    Coop save(Coop coop);
}
