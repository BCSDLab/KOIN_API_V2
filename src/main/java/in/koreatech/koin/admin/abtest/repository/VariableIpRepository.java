package in.koreatech.koin.admin.abtest.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.admin.abtest.model.redis.VariableIp;

public interface VariableIpRepository extends Repository<VariableIp, String> {

    VariableIp save(VariableIp variableIp);

    Optional<VariableIp> findById(String id);

    default Optional<VariableIp> findByVariableIdAndIp(Integer variableId, String ip) {
        return findById(variableId + ":" + ip);
    }
}
