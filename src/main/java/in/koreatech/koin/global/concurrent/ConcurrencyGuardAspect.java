package in.koreatech.koin.global.concurrent;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import in.koreatech.koin.global.concurrent.exception.ConcurrencyLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
@Profile("!test")
@RequiredArgsConstructor
public class ConcurrencyGuardAspect {

    private final RedissonClient redissonClient;
    private final TransactionAspect transactionAspect;

    @Around("@annotation(ConcurrencyGuard) && (args(..))")
    public Object handleConcurrency(ProceedingJoinPoint joinPoint) throws Throwable {
        ConcurrencyGuard annotation = getAnnotation(joinPoint);

        Object[] args = joinPoint.getArgs();

        String lockName = getLockName(args, annotation);
        RLock lock = redissonClient.getLock(lockName);

        try {
            boolean available = lock.tryLock(annotation.waitTime(), annotation.leaseTime(), annotation.timeUnit());

            if (!available) {
                throw ConcurrencyLockException.withDetail("Redisson GetLock 타임 아웃 lockName: " + lockName);
            }

            return transactionAspect.proceed(joinPoint);
        } finally {
            try {
                lock.unlock();
            } catch (IllegalMonitorStateException e) {
                log.warn("Redisson 락이 이미 해제되었습니다 lockName: " + lockName);
            }
        }
    }

    private ConcurrencyGuard getAnnotation(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        return method.getAnnotation(ConcurrencyGuard.class);
    }

    private String getLockName(Object[] args, ConcurrencyGuard annotation) {
        String lockNameFormat = "lock:%s:%s";

        String relevantParameter;
        if (args.length > 0) {
            relevantParameter = args[0].toString();
        } else {
            relevantParameter = "default";
        }

        return String.format(lockNameFormat, annotation.lockName(), relevantParameter);
    }
}
