package in.koreatech.koin.fixture;

import static in.koreatech.koin.domain.user.model.UserGender.MAN;
import static in.koreatech.koin.domain.user.model.UserType.ADMIN;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Component;

import in.koreatech.koin.admin.abtest.model.Abtest;
import in.koreatech.koin.admin.abtest.model.AbtestVariable;
import in.koreatech.koin.admin.abtest.model.AccessHistory;
import in.koreatech.koin.admin.abtest.model.AccessHistoryAbtestVariable;
import in.koreatech.koin.admin.abtest.model.Device;
import in.koreatech.koin.admin.abtest.repository.AbtestRepository;
import in.koreatech.koin.admin.abtest.repository.AbtestVariableRepository;
import in.koreatech.koin.admin.abtest.repository.AccessHistoryAbtestVariableRepository;
import in.koreatech.koin.admin.abtest.repository.AccessHistoryRepository;
import in.koreatech.koin.admin.abtest.repository.DeviceRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;

@Component
public final class AbtestFixture {

    private final AbtestRepository abtestRepository;
    private final AbtestVariableRepository abtestVariableRepository;
    private final AccessHistoryRepository accessHistoryRepository;
    private final AccessHistoryAbtestVariableRepository accessHistoryAbtestVariableRepository;
    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;

    public AbtestFixture(AbtestRepository abtestRepository, AbtestVariableRepository abtestVariableRepository,
        AccessHistoryRepository accessHistoryRepository,
        AccessHistoryAbtestVariableRepository accessHistoryAbtestVariableRepository,
        DeviceRepository deviceRepository, UserRepository userRepository) {
        this.abtestRepository = abtestRepository;
        this.abtestVariableRepository = abtestVariableRepository;
        this.accessHistoryRepository = accessHistoryRepository;
        this.accessHistoryAbtestVariableRepository = accessHistoryAbtestVariableRepository;
        this.deviceRepository = deviceRepository;
        this.userRepository = userRepository;
    }

    public Abtest 식단_UI_실험() {
        Abtest abtest =
            Abtest.builder()
                .title("dining_ui_test")
                .displayTitle("식단_UI_실험")
                .description("식단_UI_실험")
                .creator("송선권")
                .team("campus")
                .status("IN_PROGRESS")
                .build();

        AbtestVariable abtestVariable =
            AbtestVariable.builder()
                .abtest(abtest)
                .name("A")
                .displayName("실험군 A")
                .rate(50)
                .count(0)
                .isBefore(false)
                .build();

        AbtestVariable abtestVariable2 =
            AbtestVariable.builder()
                .abtest(abtest)
                .name("B")
                .displayName("실험군 B")
                .rate(50)
                .count(0)
                .isBefore(false)
                .build();

        abtest.getAbtestVariables().addAll(List.of(abtestVariable, abtestVariable2));
        return abtestRepository.save(abtest);

    }

    public Abtest 주변상점_UI_실험() {
        return abtestRepository.save(
            Abtest.builder()
                .title("shop_ui_test")
                .displayTitle("주변상점_UI_실험")
                .description("주변상점_UI_실험")
                .creator("송선권")
                .team("campus")
                .status("IN_PROGRESS")
                .build()
        );
    }

    public Device 아이폰(Integer userId) {
        return deviceRepository.save(
            Device.builder()
                .accessHistory(accessHistoryRepository.save(
                    AccessHistory.builder()
                        .publicIp("1234")
                        .build()
                ))
                .user(userRepository.getById(userId))
                .model("아이폰14")
                .os("ios 17")
                .fcmToken("abcd1234")
                .lastAccessedAt(LocalDateTime.of(2024, 8, 6, 14, 46))
                .build()
        );
    }

    public Device 갤럭시(Integer userId) {
        return deviceRepository.save(
            Device.builder()
                .accessHistory(accessHistoryRepository.save(
                    AccessHistory.builder()
                        .publicIp("5678")
                        .build()
                ))
                .user(userRepository.getById(userId))
                .model("갤럭시24")
                .os("android 17")
                .fcmToken("abcd14")
                .lastAccessedAt(LocalDateTime.of(2024, 8, 6, 14, 46))
                .build()
        );
    }
}
