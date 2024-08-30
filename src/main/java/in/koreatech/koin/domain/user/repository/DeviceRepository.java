package in.koreatech.koin.domain.user.repository;

import java.util.List;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.user.model.Device;

public interface DeviceRepository extends Repository<Device, Integer> {
    Device save(Device device);

    List<Device> findAllByUserId(Integer id);
}
