package in.koreatech.koin.domain.owner.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.owner.exception.OwnerNotFoundException;
import in.koreatech.koin.domain.owner.model.Owner;

public interface OwnerRepository extends Repository<Owner, Long> {

    Optional<Owner> findById(Long ownerId);

    default Owner getById(Long ownerId) {
        return findById(ownerId).orElseThrow(() -> OwnerNotFoundException.withDetail("ownerId: " + ownerId));
    }

    Owner save(Owner owner);
}
