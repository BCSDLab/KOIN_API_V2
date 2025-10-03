package in.koreatech.koin.admin.abtest.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.admin.abtest.exception.DeviceNotFoundException;
import in.koreatech.koin.admin.abtest.model.Device;
import in.koreatech.koin.global.config.repository.JpaRepository;

@JpaRepository
public interface DeviceRepository extends Repository<Device, Integer> {

    Device save(Device device);

    Optional<Device> findById(Integer id);

    List<Device> findAllByUserId(Integer id);

    default Device getById(Integer id) {
        return findById(id).orElseThrow(() ->
            DeviceNotFoundException.withDetail("id: " + id));
    }
}
