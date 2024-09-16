package in.koreatech.koin.admin.abtest.repository;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.admin.abtest.model.AccessHistoryAbtestVariable;

public interface AccessHistoryAbtestVariableRepository extends Repository<AccessHistoryAbtestVariable, Integer> {

    AccessHistoryAbtestVariable save(AccessHistoryAbtestVariable accessHistoryAbtestVariable);
}
