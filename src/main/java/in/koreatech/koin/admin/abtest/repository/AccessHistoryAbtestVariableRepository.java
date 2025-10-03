package in.koreatech.koin.admin.abtest.repository;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.admin.abtest.model.AccessHistoryAbtestVariable;
import in.koreatech.koin.global.config.repository.JpaRepository;

@JpaRepository
public interface AccessHistoryAbtestVariableRepository extends Repository<AccessHistoryAbtestVariable, Integer> {

    AccessHistoryAbtestVariable save(AccessHistoryAbtestVariable accessHistoryAbtestVariable);
}
