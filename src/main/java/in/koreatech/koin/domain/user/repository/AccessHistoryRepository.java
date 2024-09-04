package in.koreatech.koin.domain.user.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.user.exception.AccessHistoryNotFoundException;
import in.koreatech.koin.domain.user.model.AccessHistory;

public interface AccessHistoryRepository extends Repository<AccessHistory, Integer> {

    AccessHistory save(AccessHistory accessHistory);

    Optional<AccessHistory> findById(Integer id);

    Optional<AccessHistory> findByDeviceId(Integer deviceId);
/*
    default AccessHistory getByPublicIp(String publicIp) {
        return findById(publicIp).orElseThrow(() ->
            AccessHistoryNotFoundException.withDetail("publicIp: " + publicIp));
    }*/

    default AccessHistory getByDeviceId(Integer deviceId) {
        return findByDeviceId(deviceId).orElseThrow(() ->
            AccessHistoryNotFoundException.withDetail("deviceId: " + deviceId));
    }
}
