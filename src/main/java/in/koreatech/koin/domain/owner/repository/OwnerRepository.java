package in.koreatech.koin.domain.owner.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.owner.exception.OwnerNotFoundException;
import in.koreatech.koin.domain.owner.model.Owner;

public interface OwnerRepository extends Repository<Owner, Integer> {

    Optional<Owner> findById(Integer ownerId);

    default Owner getById(Integer ownerId) {
        return findById(ownerId).orElseThrow(() -> OwnerNotFoundException.withDetail("ownerId: " + ownerId));
    }

    Optional<Owner> findByAccount(String account);

    default Owner getByAccount(String account) {
        return findByAccount(account).orElseThrow(() -> OwnerNotFoundException.withDetail("ownerAccount : " + account));
    }

    Owner save(Owner owner);

    Optional<Owner> findByCompanyRegistrationNumber(String companyRegistrationNumber);

    void deleteByUserId(Integer userId);
}
