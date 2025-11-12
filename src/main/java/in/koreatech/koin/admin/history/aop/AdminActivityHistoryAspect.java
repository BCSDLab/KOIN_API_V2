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
import in.koreatech.koin.admin.manager.model.Admin;
import in.koreatech.koin.admin.manager.repository.AdminRepository;
import in.koreatech.koin.global.auth.AuthContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Aspect
@Component
@Profile("!test")
@RequiredArgsConstructor
public class AdminActivityHistoryAspect {

    private final AuthContext authContext;
    private final AdminRepository adminRepository;
    private final AdminActivityHistoryRepository adminActivityHistoryRepository;

    @Around("@annotation(AdminActivityLogging)")
    public Object logAdminActivity(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
        HttpMethodType requestMethod = HttpMethodType.valueOf(request.getMethod());
        ContentCachingRequestWrapper cachingRequest = (ContentCachingRequestWrapper)request;
        String requestMessage = new String(cachingRequest.getContentAsByteArray());

        Object result = joinPoint.proceed();

        Admin admin = adminRepository.getById(authContext.getUserId());
        AdminActivityLogging adminActivityLogging = getAnnotation(joinPoint);
        Integer domainId = extractDomainId(joinPoint, adminActivityLogging.domainIdParam());
        adminActivityHistoryRepository.save(AdminActivityHistory.builder()
            .domainId(domainId)
            .admin(admin)
            .requestMethod(requestMethod)
            .domainName(adminActivityLogging.domain())
            .requestMessage(requestMessage)
            .build()
        );

        return result;
    }

    private AdminActivityLogging getAnnotation(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        Method method = signature.getMethod();
        return method.getAnnotation(AdminActivityLogging.class);
    }

    private Integer extractDomainId(ProceedingJoinPoint joinPoint, String paramName) {
        if (paramName.isEmpty()) {
            return null;
        }

        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        String[] parameterNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < parameterNames.length; i++) {
            if (parameterNames[i].equals(paramName)) {
                return (Integer)args[i];
            }
        }

        return null;
    }
}
