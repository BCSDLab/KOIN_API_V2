package in.koreatech.koin.domain.owner.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.owner.model.OwnerEmailRequest;

public interface OwnerEmailRequestRedisRepository extends Repository<OwnerEmailRequest, String> {

    OwnerEmailRequest save(OwnerEmailRequest ownerEmailRequest);

    Optional<OwnerEmailRequest> findById(String email);
}
