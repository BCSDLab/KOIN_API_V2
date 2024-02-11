package in.koreatech.koin.domain.owner.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.owner.domain.Owner;
import in.koreatech.koin.domain.owner.exception.OwnerNotFoundException;

public interface OwnerRepository extends Repository<Owner, Long> {

    Optional<Owner> findById(Long ownerId);

    Owner save(Owner owner);

    default Owner getById(Long ownerId) {
        return findById(ownerId).orElseThrow(() -> OwnerNotFoundException.withDetail("Owner id: " + ownerId));
    }
}
