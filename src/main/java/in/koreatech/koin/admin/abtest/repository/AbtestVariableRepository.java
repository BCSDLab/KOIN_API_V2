package in.koreatech.koin.admin.abtest.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.admin.abtest.exception.AbtestVariableNotFoundException;
import in.koreatech.koin.admin.abtest.model.AbtestVariable;

public interface AbtestVariableRepository extends Repository<AbtestVariable, Integer> {

    Optional<AbtestVariable> findById(Integer variableId);

    default AbtestVariable getById(Integer variableId) {
        return findById(variableId).orElseThrow(() ->
            AbtestVariableNotFoundException.withDetail("AbtestVariable id: " + variableId));
    }
}
