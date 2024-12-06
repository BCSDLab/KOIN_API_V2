package in.koreatech.koin.admin.history.aop;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.ContentCachingRequestWrapper;

import in.koreatech.koin.admin.history.enums.HttpMethodType;
import in.koreatech.koin.admin.history.model.AdminActivityHistory;
import in.koreatech.koin.admin.history.repository.AdminActivityHistoryRepository;
import in.koreatech.koin.admin.user.repository.AdminRepository;
import in.koreatech.koin.global.auth.AuthContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Aspect
@Component
@Profile("!test")
@RequiredArgsConstructor
public class AdminActivityHistoryAspect {

    private static final String SEPARATOR = "/";
    private static final String REGEX_NUMERIC = "^[0-9]*$";

    private final AuthContext authContext;
    private final AdminRepository adminRepository;
    private final AdminActivityHistoryRepository adminActivityHistoryRepository;

    @Around("@annotation(AdminLogging) && (args(..))")
    public Object logAdminActivity(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
        ContentCachingRequestWrapper cachingRequest = (ContentCachingRequestWrapper)request;

        Object result = joinPoint.proceed();
        AdminLogging annotation = getAnnotation(joinPoint);

        adminActivityHistoryRepository.save(AdminActivityHistory.builder()
            .domainId(getDomainId(annotation, request.getRequestId()))
            .admin(adminRepository.getById(authContext.getUserId()))
            .requestMethod(HttpMethodType.valueOf(request.getMethod()))
            .domainName(annotation.domainType())
            .requestMessage(new String(cachingRequest.getContentAsByteArray()))
            .build()
        );

        return result;
    }

    private AdminLogging getAnnotation(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        Method method = signature.getMethod();
        return method.getAnnotation(AdminLogging.class);
    }

    private Integer getDomainId(AdminLogging annotation, String requestURI) {
        if (annotation.hasId()) {
            String[] segments = requestURI.split(SEPARATOR);

            for (int index = segments.length - 1; index >= 0; index--) {
                if (segments[index].matches(REGEX_NUMERIC)) {
                    return Integer.parseInt(segments[index]);
                }
            }
        }
        return null;
    }
}
