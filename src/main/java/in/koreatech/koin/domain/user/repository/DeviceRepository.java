package in.koreatech.koin.domain.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.user.exception.DeviceNotFoundException;
import in.koreatech.koin.domain.user.model.Device;

public interface DeviceRepository extends Repository<Device, Integer> {
    Device save(Device device);

    Optional<Device> findByUserId(Integer userId);

    default Device getByUserId(Integer userId) {
        return findByUserId(userId).orElseThrow(() ->
            DeviceNotFoundException.withDetail("userId: " + userId));
    }

    List<Device> findAllByUserId(Integer id);
}
