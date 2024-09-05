package in.koreatech.koin.fixture;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import in.koreatech.koin.admin.abtest.model.AccessHistory;
import in.koreatech.koin.admin.abtest.model.Device;
import in.koreatech.koin.admin.abtest.repository.AccessHistoryRepository;
import in.koreatech.koin.admin.abtest.repository.DeviceRepository;
import in.koreatech.koin.domain.user.repository.UserRepository;

@Component
public class DeviceFixture {

    private final AccessHistoryRepository accessHistoryRepository;
    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;

    public DeviceFixture(
        AccessHistoryRepository accessHistoryRepository,
        DeviceRepository deviceRepository,
        UserRepository userRepository
    ) {
        this.accessHistoryRepository = accessHistoryRepository;
        this.deviceRepository = deviceRepository;
        this.userRepository = userRepository;
    }

    public Device 아이폰(Integer userId, String publicIp, String fcmToken) {
        AccessHistory accessHistory = AccessHistory.builder()
            .lastAccessedAt(LocalDateTime.of(2024, 8, 6, 14, 46))
            .build();
        Device device = Device.builder()
            .accessHistory(accessHistory)
            .user(userRepository.getById(userId))
            .model("아이폰14")
            .type("mobile")
            .build();
        accessHistory.connectDevice(device);
        return deviceRepository.save(device);
    }

    public Device 갤럭시(Integer userId, String publicIp, String fcmToken) {
        AccessHistory accessHistory = AccessHistory.builder()
            .lastAccessedAt(LocalDateTime.of(2024, 8, 6, 14, 46))
            .build();
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
