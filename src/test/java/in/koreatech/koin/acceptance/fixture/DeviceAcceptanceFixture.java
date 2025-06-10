package in.koreatech.koin.acceptance.fixture;

import org.springframework.stereotype.Component;

import in.koreatech.koin.admin.abtest.model.AccessHistory;
import in.koreatech.koin.admin.abtest.model.Device;
import in.koreatech.koin.admin.abtest.repository.DeviceRepository;
import in.koreatech.koin.domain.user.repository.UserRepository;

@Component
public class DeviceAcceptanceFixture {

    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;

    public DeviceAcceptanceFixture(
        DeviceRepository deviceRepository,
        UserRepository userRepository
    ) {
        this.deviceRepository = deviceRepository;
        this.userRepository = userRepository;
    }

    public Device 아이폰(Integer userId) {
        AccessHistory accessHistory = AccessHistory.builder().build();
        Device device = Device.builder()
            .accessHistory(accessHistory)
            .user(userRepository.getById(userId))
            .model("아이폰14")
            .type("mobile")
            .build();
        accessHistory.connectDevice(device);
        return deviceRepository.save(device);
    }

    public Device 갤럭시(Integer userId) {
        AccessHistory accessHistory = AccessHistory.builder().build();
        Device device = Device.builder()
            .accessHistory(accessHistory)
            .user(userRepository.getById(userId))
            .model("갤럭시24")
            .type("mobile")
            .build();
        accessHistory.connectDevice(device);
        return deviceRepository.save(device);
    }
}
