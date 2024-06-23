package in.koreatech.koin.admin.user.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.owner.model.OwnerShop;

public interface AdminOwnerShopRedisRepository extends Repository<OwnerShop, Integer> {

    Optional<OwnerShop> findById(Integer ownerId);
}
