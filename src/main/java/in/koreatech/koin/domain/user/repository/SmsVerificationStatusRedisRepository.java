package in.koreatech.koin.domain.user.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.user.model.SmsVerificationStatus;
import in.koreatech.koin.integration.email.exception.VerifyNotFoundException;

public interface SmsVerificationStatusRedisRepository extends Repository<SmsVerificationStatus, String> {

    SmsVerificationStatus save(SmsVerificationStatus userInVerification);

    Optional<SmsVerificationStatus> findByPhoneNumber(String phoneNumber);

    void deleteByPhoneNumber(String phoneNumber);

    default SmsVerificationStatus getByPhoneNumber(String phoneNumber) {
        return findByPhoneNumber(phoneNumber).orElseThrow(
            () -> VerifyNotFoundException.withDetail("verify: " + phoneNumber));
    }
}
