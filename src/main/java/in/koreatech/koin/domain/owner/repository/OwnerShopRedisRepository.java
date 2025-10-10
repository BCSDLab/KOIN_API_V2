package in.koreatech.koin.domain.owner.repository;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.owner.model.OwnerShop;
import in.koreatech.koin.global.marker.RedisRepositoryMarker;

@RedisRepositoryMarker
public interface OwnerShopRedisRepository extends Repository<OwnerShop, Integer> {

    OwnerShop save(OwnerShop ownerShop);

    OwnerShop findById(Integer ownerId);
}
