package in.koreatech.koin.global.domain.callcontol;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import in.koreatech.koin.domain.bus.util.ExpressBusClient;
import in.koreatech.koin.global.domain.callcontol.exception.BeanNotFoundException;
import in.koreatech.koin.global.domain.callcontol.exception.MethodNotFoundException;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class CallControlConfig {

    private final ApplicationContext context;
    private final CallControlRegistry callControlRegistry;
    private final Class<CallControlInfo> CALL_CONTROL_INFO = CallControlInfo.class;

    @Bean
    public void registerExpressBusApiCallControl() {
        callControlRegistry.registerCallControl("ExpressBusClient", generateCallControl(ExpressBusClient.class));
    }

    public CallControl generateCallControl(Class<?> superType) {
        List<ApiInfo> apiInfos = new ArrayList<>();
        List<?> proxyClasses = generateProxyClassList(superType);
        int total = 0;
        for (var proxyClass : proxyClasses) {
            Class<?> targetClass = AopProxyUtils.ultimateTargetClass(proxyClass);
            int end = total + getClassRatio(targetClass);
            apiInfos.add(generateApiInfo(targetClass, proxyClass, total, end));
            total = end;
        }
        return new CallControl(apiInfos, total);
    }

    public ApiInfo generateApiInfo(Class<?> targetClass, Object proxyClass, int start, int end) {
        return ApiInfo.builder()
            .name(targetClass.getSimpleName())
            .targetClass(proxyClass)
            .targetMethod(getTargetMethod(targetClass))
            .start(start)
            .end(end)
            .build();
    }

    public List<?> generateProxyClassList(Class<?> superType) {
        try {
            return context.getBeansOfType(superType).values().stream().toList();
        } catch (NoSuchBeanDefinitionException e) {
            throw new BeanNotFoundException("하위 타입의 Bean이 존재하지 않습니다. 상위 타입: " + superType);
        }
    }

    public int getClassRatio(Class<?> targetClass) {
        try {
            return targetClass.getAnnotation(CALL_CONTROL_INFO).ratio();
        } catch (Exception e) {
            throw new BeanNotFoundException("클래스가 해당 어노테이션을 가지지 않습니다. annotation : CallControlInfo");
        }
    }

    public Method getTargetMethod(Class<?> targetClass) {
        String targetMethod = targetClass.getAnnotation(CALL_CONTROL_INFO).targetMethod();
        try {
            return targetClass.getMethod(targetMethod);
        } catch (NoSuchMethodException e) {
            throw new MethodNotFoundException("클래스 내에 해당 메서드가 존재하지 않습니다. method : " + targetMethod);
        }
    }

}
