package in.koreatech.koin.admin.user.repository;

import java.util.List;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.shop.model.Shop;

public interface AdminShopRepository extends Repository<Shop, Integer> {

    List<Shop> findAllByOwnerId(Integer ownerId);
}
