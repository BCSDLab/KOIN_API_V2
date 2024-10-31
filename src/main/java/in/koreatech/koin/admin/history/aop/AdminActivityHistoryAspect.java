package in.koreatech.koin.admin.history.aop;

import org.apache.commons.lang3.EnumUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.ContentCachingRequestWrapper;

import in.koreatech.koin.admin.history.enums.DomainType;
import in.koreatech.koin.admin.history.model.AdminActivityHistory;
import in.koreatech.koin.admin.history.repository.AdminActivityHistoryRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.auth.AuthContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Aspect
@Component
@Profile("!test")
@RequiredArgsConstructor
public class AdminActivityHistoryAspect {

    private final AuthContext authContext;
    private final UserRepository userRepository;
    private final AdminActivityHistoryRepository adminActivityHistoryRepository;
    private final String regex = "^[0-9]*$";

    @Pointcut("execution(* in.koreatech.koin.admin..controller.*.*(..))")
    private void allAdminControllers() {
    }

    @Pointcut("!@annotation(org.springframework.web.bind.annotation.GetMapping)")
    private void excludeGetMapping() {
    }

    @Pointcut("!execution(* in.koreatech.koin.admin.user.controller.AdminUserController.adminLogin(..)) && "
        + "!execution(* in.koreatech.koin.admin.user.controller.AdminUserController.logout(..)) && "
        + "!execution(* in.koreatech.koin.admin.user.controller.AdminUserController.refresh(..)) && "
        + "!execution(* in.koreatech.koin.admin.abtest.controller.AbtestController.assignOrGetAbtestVariable(..))")
    private void excludeSpecificMethods() {
    }

    @Around("allAdminControllers() && excludeGetMapping() && excludeSpecificMethods()")
    public Object logAdminActivity(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
        String requestURI = request.getRequestURI();
        String requestMethod = request.getMethod();

        ContentCachingRequestWrapper cachingRequest = (ContentCachingRequestWrapper)request;
        String requestMessage = new String(cachingRequest.getContentAsByteArray());

        Object result = joinPoint.proceed();

        User user = userRepository.getById(authContext.getUserId());
        DomainInfo domainInfo = getDomainInfo(requestURI);

        adminActivityHistoryRepository.save(AdminActivityHistory.builder()
            .domainId(domainInfo.domainId())
            .user(user)
            .requestMethod(requestMethod)
            .domainName(domainInfo.domainName())
            .requestMessage(requestMessage)
            .build());

        return result;
    }

    private DomainInfo getDomainInfo(String requestURI) {
        String[] split = requestURI.split("/");
        Integer domainId = null;
        String domainName = null;

        for (int i = split.length - 1; i >= 0; i--) {
            String segment = split[i];
            if (EnumUtils.isValidEnumIgnoreCase(DomainType.class, segment) && domainName == null) {
                domainName = segment.toUpperCase();
                if (i != split.length - 1) {
                    String index = split[i + 1];
                    if (index.matches(regex) && domainId == null) {
                        domainId = Integer.valueOf(index);
                    }
                }
                break;
            }
        }

        return new DomainInfo(domainId, domainName);
    }

    private record DomainInfo(Integer domainId, String domainName) {
    }
}
