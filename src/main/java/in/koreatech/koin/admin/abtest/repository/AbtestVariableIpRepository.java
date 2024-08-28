package in.koreatech.koin.admin.abtest.repository;

import static in.koreatech.koin.admin.abtest.model.redis.AbtestVariableIp.DELIMITER;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.admin.abtest.model.redis.AbtestVariableIp;

public interface AbtestVariableIpRepository extends Repository<AbtestVariableIp, String> {

    AbtestVariableIp save(AbtestVariableIp abtestVariableIp);

    Optional<AbtestVariableIp> findById(String id);

    default Optional<AbtestVariableIp> findByVariableIdAndIp(Integer variableId, String ip) {
        return findById(variableId + DELIMITER + ip);
    }
}
