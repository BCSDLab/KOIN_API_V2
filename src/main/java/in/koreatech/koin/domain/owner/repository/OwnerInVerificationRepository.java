package in.koreatech.koin.domain.owner.repository;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.owner.model.OwnerInVerification;

public interface OwnerInVerificationRepository extends Repository<OwnerInVerification, String> {

    OwnerInVerification save(OwnerInVerification ownerInVerification);
}
