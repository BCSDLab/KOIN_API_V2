package in.koreatech.koin.admin.owner.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.owner.model.OwnerShop;
import in.koreatech.koin.global.config.repository.JpaRepository;

@JpaRepository
public interface AdminOwnerShopRedisRepository extends Repository<OwnerShop, Integer> {

    Optional<OwnerShop> findById(Integer ownerId);

    void deleteById(Integer ownerId);
}
