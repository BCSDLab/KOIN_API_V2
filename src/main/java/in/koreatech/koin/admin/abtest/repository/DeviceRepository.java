package in.koreatech.koin.admin.abtest.repository;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.admin.abtest.model.Device;

public interface DeviceRepository extends Repository<Device, Integer> {
    Device save(Device device);
}
