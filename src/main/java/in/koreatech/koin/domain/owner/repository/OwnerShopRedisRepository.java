package in.koreatech.koin.domain.owner.repository;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.owner.model.OwnerShop;

public interface OwnerShopRedisRepository extends Repository<OwnerShop, Integer> {

    OwnerShop save(OwnerShop ownerShop);

    OwnerShop findById(Integer ownerId);
}
