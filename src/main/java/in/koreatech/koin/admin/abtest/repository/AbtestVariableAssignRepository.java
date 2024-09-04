package in.koreatech.koin.admin.abtest.repository;

import static in.koreatech.koin.admin.abtest.model.redis.AbtestVariableAssign.DELIMITER;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.admin.abtest.model.redis.AbtestVariableAssign;

public interface AbtestVariableAssignRepository extends Repository<AbtestVariableAssign, String> {

    AbtestVariableAssign save(AbtestVariableAssign abtestVariableAssign);

    Optional<AbtestVariableAssign> findById(String id);

    void deleteById(String id);

    default Optional<AbtestVariableAssign> findByVariableIdAndIp(Integer variableId, Integer accessHistoryId) {
        return findById(variableId + DELIMITER + accessHistoryId);
    }

    default void deleteByVariableIdAndIp(Integer variableId, Integer accessHistoryId) {
        deleteById(variableId + DELIMITER + accessHistoryId);
    }
}
