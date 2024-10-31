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

    private static final String REGEX_NUMERIC = "^[0-9]*$";
    private static final String SEGMENT_SHOPS = "SHOPS";
    private static final String SEGMENT_BENEFIT = "benefit";
    private static final String SEGMENT_CATEGORIES = "CATEGORIES";
    private static final String SEGMENT_CLOSE = "close";
    private static final String SEGMENT_ABTEST = "abtest";

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
        String[] segments = requestURI.split("/");
        Integer domainId = null;
        String domainName = null;

        for (int i = segments.length - 1; i >= 0; i--) {
            String segment = segments[i];

            if (isDomainType(segment)) {
                domainName = getDomainName(segment, segments, i);
                domainId = getDomainId(segments, i);
                break;
            }

            if (isCloseAbtest(segment, segments, i)) {
                domainName = segments[i - 1].toUpperCase();
                domainId = Integer.valueOf(segments[i + 1]);
                break;
            }
        }

        return new DomainInfo(domainId, domainName);
    }

    private boolean isDomainType(String segment) {
        return EnumUtils.isValidEnumIgnoreCase(DomainType.class, segment);
    }

    private String getDomainName(String segment, String[] segments, int index) {
        String domainName = segment.toUpperCase();

        if (SEGMENT_SHOPS.equals(domainName) && SEGMENT_BENEFIT.equals(segments[index - 2])) {
            return segments[index - 2].toUpperCase();
        }

        if (SEGMENT_CATEGORIES.equals(domainName)) {
            return (segments[index - 1] + domainName).toUpperCase();
        }

        return domainName;
    }

    private Integer getDomainId(String[] segments, int index) {
        if (index != segments.length - 1 && segments[index + 1].matches(REGEX_NUMERIC)) {
            return Integer.valueOf(segments[index + 1]);
        }
        return null;
    }

    private boolean isCloseAbtest(String segment, String[] segments, int index) {
        return SEGMENT_CLOSE.equals(segment) && SEGMENT_ABTEST.equals(segments[index - 1]);
    }

    private record DomainInfo(Integer domainId, String domainName) {
    }
}
