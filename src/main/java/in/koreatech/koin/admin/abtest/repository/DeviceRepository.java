package in.koreatech.koin.admin.abtest.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.admin.abtest.exception.DeviceNotFoundException;
import in.koreatech.koin.admin.abtest.model.Device;

public interface DeviceRepository extends Repository<Device, Integer> {
    Device save(Device device);

    Optional<Device> findByUserId(Integer userId);

    default Device getByUserId(Integer userId) {
        return findByUserId(userId).orElseThrow(() ->
            DeviceNotFoundException.withDetail("userId: " + userId));
    }
}
