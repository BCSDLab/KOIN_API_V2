package in.koreatech.koin.admin.abtest.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.admin.abtest.exception.AccessHistoryNotFoundException;
import in.koreatech.koin.admin.abtest.model.AccessHistory;
import in.koreatech.koin.admin.abtest.model.Device;

public interface AccessHistoryRepository extends Repository<AccessHistory, Integer> {

    AccessHistory save(AccessHistory accessHistory);

    Optional<AccessHistory> findByPublicIp(String publicIp);

    Optional<AccessHistory> findByDevice(Device device);

    Optional<AccessHistory> findByDeviceId(Integer deviceId);

    default AccessHistory getByPublicIp(String publicIp) {
        return findByPublicIp(publicIp).orElseThrow(() ->
            AccessHistoryNotFoundException.withDetail("publicIp: " + publicIp));
    }

    default AccessHistory getByDeviceId(Integer deviceId) {
        return findByDeviceId(deviceId).orElseThrow(() ->
            AccessHistoryNotFoundException.withDetail("deviceId: " + deviceId));
    }
}
