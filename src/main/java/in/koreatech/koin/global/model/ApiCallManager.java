package in.koreatech.koin.global.model;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

import in.koreatech.koin.KoinApplication;
import in.koreatech.koin.global.domain.random.model.RandomGenerator;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ApiCallManager<T> {

    private final Class<T> classType;
    private final Map<Class<?>, List<Integer>> apiInfos = new HashMap<>();
    private final ApplicationContext context = new AnnotationConfigApplicationContext(KoinApplication.class);
    private int totalRatio = 0;

    public void callApi() {
        List<T> apiList = generateApiList(context);
        generateApiInfoList(apiList);
        Class<?> selected = selectClass(RandomGenerator.createNumber(0, totalRatio));
        callMethod(selected);
    }

    public List<T> generateApiList(ApplicationContext applicationContext) {
        try {
            return applicationContext.getBeansOfType(classType).values().stream().toList();
        } catch (NoSuchBeanDefinitionException e) {
            throw new IllegalArgumentException("해당 타입의 하위 클래스가 존재하지 않습니다. 타입: " + classType);
        }
    }

    public void generateApiInfoList(List<T> apiList) {
        apiList
            .forEach(bean -> {
                Class<?> apiClass = AopProxyUtils.ultimateTargetClass(bean);
                List<Integer> ratioList = new ArrayList<>();
                ratioList.add(totalRatio);
                totalRatio += apiClass.getAnnotation(ApiCallConfig.class).ratio();
                ratioList.add(totalRatio);
                this.apiInfos.put(apiClass, ratioList);
            });
    }

    public Class<?> selectClass(int randomNum) {
        return apiInfos.entrySet().stream()
            .filter(apiInfo -> apiInfo.getValue().get(0) <= randomNum && apiInfo.getValue().get(1) > randomNum)
            .findAny()
            .map(Map.Entry::getKey)
            .orElseThrow(() -> new IllegalArgumentException("잘못된 범위의 숫자입니다."));
    }

    private void callMethod(Class<?> selected) {
        Method method = getMethodName(selected);
        try {
            System.out.println("선택된 클래스 : " + selected.getName());
            System.out.println("선택된 메서드 : " + method.getName());
            method.invoke(selected);
        } catch (Exception e) {
            throw new RuntimeException("Api 호출에 실패하였습니다.");
        }
    }

    public Method getMethodName(Class<?> apiClass) {
        String methodName = apiClass.getAnnotation(ApiCallConfig.class).methodToCall();
        try {
            return apiClass.getMethod(methodName);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("존재하지 않는 메서드 입니다. method : " + methodName);
        }
    }

    public void varifyCircuitBreakerStatus() {

    }
}
