package in.koreatech.koin.domain.order.address.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import in.koreatech.koin.domain.order.address.model.RiderMessage;

public interface RiderMessageRepository extends JpaRepository<RiderMessage, Integer> {
}
