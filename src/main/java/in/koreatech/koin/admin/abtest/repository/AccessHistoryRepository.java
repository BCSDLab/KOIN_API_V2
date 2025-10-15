package in.koreatech.koin.admin.abtest.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.admin.abtest.exception.AccessHistoryNotFoundException;
import in.koreatech.koin.admin.abtest.model.AccessHistory;

public interface AccessHistoryRepository extends Repository<AccessHistory, Integer> {

    AccessHistory save(AccessHistory accessHistory);

    Optional<AccessHistory> findById(Integer id);

    Optional<AccessHistory> findByDeviceId(Integer deviceId);

    default AccessHistory getById(Integer accessHistoryId) {
        return findById(accessHistoryId).orElseThrow(() ->
            AccessHistoryNotFoundException.withDetail("accessHistoryId: " + accessHistoryId));
    }

    default AccessHistory getByDeviceId(Integer deviceId) {
        return findByDeviceId(deviceId).orElseThrow(() ->
            AccessHistoryNotFoundException.withDetail("deviceId: " + deviceId));
    }
}
