package in.koreatech.koin.global.duplicate;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.digest.DigestUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;
import lombok.RequiredArgsConstructor;

@Aspect
@Component
@RequiredArgsConstructor
public class DuplicateGuardAspect {

    private final RedisTemplate<String, String> redisTemplate;
    private final SpelExpressionParser parser;
    private final DefaultParameterNameDiscoverer parameterNameDiscoverer;

    private static final String IN_PROGRESS = "IN_PROGRESS";

    @Around("@annotation(duplicateGuard)")
    public Object handleIdempotentRequest(ProceedingJoinPoint joinPoint, DuplicateGuard duplicateGuard) throws Throwable {
        String idempotencyKey = createKey(joinPoint, duplicateGuard.key());

        if (idempotencyKey == null) {
            return joinPoint.proceed();
        }

        Boolean isFirstRequest = redisTemplate.opsForValue()
            .setIfAbsent(idempotencyKey, IN_PROGRESS, duplicateGuard.timeoutSeconds(), TimeUnit.MILLISECONDS);

        if (Boolean.FALSE.equals(isFirstRequest)) {
            throw CustomException.of(ApiResponseCode.REQUEST_TOO_FAST);
        }

        return joinPoint.proceed();
    }

    private String createKey(ProceedingJoinPoint joinPoint, String spELExpression) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();
        String[] paramNames = parameterNameDiscoverer.getParameterNames(method);

        StandardEvaluationContext context = new StandardEvaluationContext();
        if (paramNames != null) {
            for (int i = 0; i < paramNames.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
        }

        try {
            Expression expression = parser.parseExpression(spELExpression);
            String rawKey = expression.getValue(context, String.class);
            return DigestUtils.sha256Hex(rawKey);
        } catch (Exception e) {
            return null;
        }
    }
}
