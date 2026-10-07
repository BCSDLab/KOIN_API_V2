package in.koreatech.koin.domain.dining.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.dining.model.DiningReportDelivery;

public interface DiningReportDeliveryRepository extends Repository<DiningReportDelivery, UUID> {

    DiningReportDelivery save(DiningReportDelivery delivery);

    Optional<DiningReportDelivery> findById(UUID id);
}
