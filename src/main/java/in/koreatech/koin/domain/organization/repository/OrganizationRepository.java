package in.koreatech.koin.domain.organization.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.organization.model.Organization;

public interface OrganizationRepository extends Repository<Organization, Integer> {

    Optional<Organization> findByUserIdAndIsDeletedFalse(Integer userId);
}
