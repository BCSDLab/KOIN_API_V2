package in.koreatech.koin.domain.bus.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import in.koreatech.koin.domain.bus.model.Bus;

public interface BusRepository extends MongoRepository<Bus, String> {

    List<Bus> findAll();
}
