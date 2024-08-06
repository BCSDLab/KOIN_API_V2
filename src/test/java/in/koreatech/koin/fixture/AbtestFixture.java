package in.koreatech.koin.fixture;

import org.springframework.stereotype.Component;

import in.koreatech.koin.admin.abtest.model.Abtest;
import in.koreatech.koin.admin.abtest.model.Device;
import in.koreatech.koin.admin.abtest.repository.AbtestRepository;
import in.koreatech.koin.admin.abtest.repository.AbtestVariableRepository;
import in.koreatech.koin.admin.abtest.repository.AccessHistoryAbtestVariableRepository;
import in.koreatech.koin.admin.abtest.repository.AccessHistoryRepository;
import in.koreatech.koin.admin.abtest.repository.DeviceRepository;
import in.koreatech.koin.domain.user.repository.UserRepository;

@Component
public final class AbtestFixture {

    private final AbtestRepository abtestRepository;
    private final AbtestVariableRepository abtestVariableRepository;
    private final AccessHistoryRepository accessHistoryRepository;
    private final AccessHistoryAbtestVariableRepository accessHistoryAbtestVariableRepository;
    private final DeviceRepository deviceRepository;

    public AbtestFixture(AbtestRepository abtestRepository, AbtestVariableRepository abtestVariableRepository,
        AccessHistoryRepository accessHistoryRepository,
        AccessHistoryAbtestVariableRepository accessHistoryAbtestVariableRepository,
        DeviceRepository deviceRepository) {
        this.abtestRepository = abtestRepository;
        this.abtestVariableRepository = abtestVariableRepository;
        this.accessHistoryRepository = accessHistoryRepository;
        this.accessHistoryAbtestVariableRepository = accessHistoryAbtestVariableRepository;
        this.deviceRepository = deviceRepository;
    }

    public Abtest 식단_UI_실험() {
        return null;
    }

    public Abtest 주변상점_UI_실험() {
        return null;
    }

    public Device 아이폰(Integer userId) {
        return null;
    }
}
