package in.koreatech.koin.domain.community.util;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.community.service.CommunityService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ValidationScheduler {

    private final CommunityService communityService;

    @Scheduled(cron = "0 0 * * * *") /* every hour */
    public void validateHits() {
        communityService.validateHits();
    }
}
