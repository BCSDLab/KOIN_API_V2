package in.koreatech.koin.domain.order.cart.aop;

import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.digest.DigestUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import in.koreatech.koin._common.code.ApiResponseCode;
import in.koreatech.koin._common.exception.CustomException;
import in.koreatech.koin.domain.order.cart.dto.CartAddItemRequest;
import lombok.RequiredArgsConstructor;

@Aspect
@Component
@RequiredArgsConstructor
public class AddItemIdempotencyAspect {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String IN_PROGRESS = "IN_PROGRESS";

    @Around("@annotation(idempotent)")
    public Object handleIdempotentRequest(ProceedingJoinPoint joinPoint, AddItemIdempotent idempotent) throws Throwable {
        String idempotencyKey = createKey(joinPoint);

        Boolean isFirstRequest = redisTemplate.opsForValue()
            .setIfAbsent(idempotencyKey, IN_PROGRESS, idempotent.timeoutSeconds(), TimeUnit.MILLISECONDS);

        if (Boolean.FALSE.equals(isFirstRequest)) {
            throw CustomException.of(ApiResponseCode.REQUEST_TOO_FAST);
        }

        return joinPoint.proceed();
    }

    private String createKey(ProceedingJoinPoint joinPoint) {
        Integer userId = null;
        CartAddItemRequest requestBody = null;

        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof Integer) {
                userId = (Integer) arg;
            } else if (arg instanceof CartAddItemRequest) {
                requestBody = (CartAddItemRequest) arg;
            }
        }

        if (userId == null || requestBody == null) {
            throw CustomException.of(ApiResponseCode.ILLEGAL_ARGUMENT);
        }

        String rawKey = "cart/add:" + userId + ":" + requestBody;
        return DigestUtils.sha256Hex(rawKey);
    }
}
