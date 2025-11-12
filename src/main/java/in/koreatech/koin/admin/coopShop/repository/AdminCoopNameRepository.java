package in.koreatech.koin.admin.coopShop.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.coopshop.model.CoopName;

public interface AdminCoopNameRepository extends Repository<CoopName, Integer> {

    Optional<CoopName> findByName(String name);
}
