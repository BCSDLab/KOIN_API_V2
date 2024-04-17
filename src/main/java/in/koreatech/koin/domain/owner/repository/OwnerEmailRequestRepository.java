package in.koreatech.koin.domain.owner.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.owner.model.OwnerEmailRequest;
import in.koreatech.koin.domain.owner.model.OwnerInVerification;

public interface OwnerEmailRequestRepository extends Repository<OwnerEmailRequest, String> {

    OwnerInVerification save(OwnerEmailRequest ownerInVerification);

    Optional<OwnerEmailRequest> findById(String email);
}
