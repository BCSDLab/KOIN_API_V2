package in.koreatech.koin.domain.shop.cache.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import in.koreatech.koin._common.event.ShopsCacheRefreshEvent;
import lombok.RequiredArgsConstructor;

@Aspect
@Component
@RequiredArgsConstructor
@Profile("!test")
public class RefreshShopsCacheAspect {

    private final ApplicationEventPublisher eventPublisher;

    @AfterReturning("@annotation(RefreshShopsCache)")
    public void publishShopsCacheRefreshEvent(JoinPoint joinPoint) {
        eventPublisher.publishEvent(new ShopsCacheRefreshEvent());
    }
}
