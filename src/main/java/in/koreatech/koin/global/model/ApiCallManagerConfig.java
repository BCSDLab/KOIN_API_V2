package in.koreatech.koin.global.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import in.koreatech.koin.domain.bus.util.ExpressBusClient;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class ApiCallManagerConfig {

    private final ApplicationContext context;
    private final ApiCallManagerRegistry apiCallManagerRegistry;

    @Bean
    public ApiCallInfo generateBusApiCallManager() {
        return apiCallManagerRegistry.apiCallInfo("ExpressBusClient", generateApiCallInfo(ExpressBusClient.class));
    }

    /* 다중화 하려는 클래스의 구현체에 어노테이션을 달아 호출비율 설정
     * generateApiCallInfo()에 다중화 하려는 클래스를 넣고 ApiCallInfo 객체 생성 후 Bean 등록
     * 등록된 ApiCallInfo를 가져와 런타임에 호출 제어 모듈로 사용
     */

    public ApiCallInfo generateApiCallInfo(Class<?> classType) {
        ApiCallInfo apiInfos = new ApiCallInfo();
        List<?> apiList = generateApiList(classType);
        for (var apiBean : apiList) {
            Class<?> apiClass = AopProxyUtils.ultimateTargetClass(apiBean);
            int apiRatio = apiClass.getAnnotation(ApiCallAnnotation.class).ratio();
            List<Integer> ratioList = new ArrayList<>();
            ratioList.add(apiInfos.getTotalRatio());
            apiInfos.sumRatio(apiRatio);
            ratioList.add(apiInfos.getTotalRatio());
            apiInfos.getApiInfos().put(apiClass, ratioList);
        }
        return apiInfos;
    }

    public List<?> generateApiList(Class<?> classType) {
        try {
            return context.getBeansOfType(classType).values().stream().toList();
        } catch (NoSuchBeanDefinitionException e) {
            throw new IllegalArgumentException("해당 타입의 하위 클래스가 존재하지 않습니다. 타입: " + classType);
        }
    }

}
