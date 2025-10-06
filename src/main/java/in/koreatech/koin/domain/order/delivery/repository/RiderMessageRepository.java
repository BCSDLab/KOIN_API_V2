package in.koreatech.koin.domain.order.delivery.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import in.koreatech.koin.domain.order.delivery.model.RiderMessage;

@in.koreatech.koin.config.repository.JpaRepository
public interface RiderMessageRepository extends JpaRepository<RiderMessage, Integer> {
}
