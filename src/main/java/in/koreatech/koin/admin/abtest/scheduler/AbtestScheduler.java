package in.koreatech.koin.admin.abtest.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import in.koreatech.koin.admin.abtest.service.AbtestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AbtestScheduler {

    private final AbtestService abtestService;

    @Scheduled(cron = "0 0 * * * *")
    // @Scheduled(cron = "0,10,20,30,40,50 * * * * *")
    public void syncCacheCountToDB() {
        // try {
            abtestService.syncCacheCountToDB();
        // } catch (Exception e) {
        //     log.warn("AB test 편입 수 DB 동기화 과정에서 오류가 발생했습니다.");
        // }
    }
}
