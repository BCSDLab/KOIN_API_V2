package in.koreatech.koin.domain.owner.repository;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.owner.domain.Owner;

public interface OwnerRepository extends Repository<Owner, Long> {

    Owner findById(Long ownerId);

    Owner save(Owner owner);
}
