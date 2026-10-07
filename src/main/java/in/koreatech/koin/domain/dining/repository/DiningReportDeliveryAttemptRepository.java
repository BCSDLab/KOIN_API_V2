package in.koreatech.koin.domain.dining.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.dining.model.DiningReportDeliveryAttempt;
import in.koreatech.koin.domain.dining.model.DiningReportDeliveryMode;

public interface DiningReportDeliveryAttemptRepository extends Repository<DiningReportDeliveryAttempt, UUID> {

    DiningReportDeliveryAttempt save(DiningReportDeliveryAttempt attempt);

    Optional<DiningReportDeliveryAttempt> findById(UUID token);

    List<DiningReportDeliveryAttempt> findByDeliveryIdAndSendAttemptTokenAndMode(UUID deliveryId,
        UUID sendAttemptToken, DiningReportDeliveryMode mode);
}
