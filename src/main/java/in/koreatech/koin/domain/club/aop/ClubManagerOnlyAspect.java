package in.koreatech.koin.domain.club.aop;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.club.club.repository.ClubManagerRepository;
import in.koreatech.koin.global.auth.exception.AuthorizationException;
import lombok.RequiredArgsConstructor;

@Aspect
@Component
@RequiredArgsConstructor
public class ClubManagerOnlyAspect {

    private final ClubManagerRepository clubManagerRepository;

    @Before(value = "@annotation(ClubManagerOnly) && args(clubId,..,studentId)", argNames = "clubId,studentId")
    public void checkClubManager(Integer clubId, Integer studentId) {
        if (!clubManagerRepository.existsByClubIdAndUserId(clubId, studentId)) {
            throw AuthorizationException.withDetail("studentId: " + studentId);
        }
    }
}
