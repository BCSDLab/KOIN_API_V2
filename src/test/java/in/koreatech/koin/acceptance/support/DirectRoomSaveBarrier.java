package in.koreatech.koin.acceptance.support;

import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentChatRoomType.DIRECT;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentChatRoom;

/**
 * DIRECT 채팅방 insert 직전에 동시 요청을 모아 경합을 재현한다.
 * {@link #expectInsertions(int)}를 호출하지 않으면 대기하지 않는다.
 */
@Aspect
public class DirectRoomSaveBarrier {

    private final AtomicReference<CountDownLatch> insertions =
        new AtomicReference<>(new CountDownLatch(0));

    public void expectInsertions(int requestCount) {
        insertions.set(new CountDownLatch(requestCount));
    }

    public void reset() {
        insertions.set(new CountDownLatch(0));
    }

    @Around("bean(teamRecruitmentChatRoomRepository) && execution(* save(..)) && args(chatRoom)")
    Object awaitConcurrentInsertions(
        ProceedingJoinPoint joinPoint,
        TeamRecruitmentChatRoom chatRoom
    ) throws Throwable {
        if (chatRoom.getRoomType() == DIRECT) {
            CountDownLatch latch = insertions.get();
            latch.countDown();
            // 수정 전에는 모든 요청이 insert까지 도달하고, 수정 후에는 최초 요청만 도달한다.
            latch.await(1, SECONDS);
        }
        return joinPoint.proceed();
    }
}
