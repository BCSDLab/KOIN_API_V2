package in.koreatech.koin.domain.owner.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.owner.exception.OwnerNotFoundException;
import in.koreatech.koin.domain.owner.model.Owner;

//TODO: 안쓰는 레포지토리들 정리하기 (이거말하는게 아니고 전체적으로 찾아서 정리하기)
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
