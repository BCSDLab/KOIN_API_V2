package in.koreatech.koin.domain.user.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.user.model.SmsAuthedStatus;
import in.koreatech.koin._common.auth.exception.AuthenticationException;

public interface SmsAuthedStatusRedisRepository extends Repository<SmsAuthedStatus, String> {

    SmsAuthedStatus save(SmsAuthedStatus smsAuthedStatus);

    Optional<SmsAuthedStatus> findByPhoneNumber(String phoneNumber);

    void deleteByPhoneNumber(String phoneNumber);

    default SmsAuthedStatus getByPhoneNumber(String phoneNumber) {
        return findByPhoneNumber(phoneNumber)
            .orElseThrow(() -> AuthenticationException.withDetail("미인증된 휴대폰 번호입니다."));
    }
}
