package in.koreatech.koin.global.model;

import java.lang.reflect.Method;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import in.koreatech.koin.global.domain.random.model.RandomGenerator;

@Component
public class ApiCallManager {

    private final ApiCallInfo apiCallInfo;
    private final ApplicationContext context;

    public ApiCallManager(ApiCallInfo apiCallInfo, ApplicationContext context) {
        this.apiCallInfo = apiCallInfo;
        this.context = context;
    }

    public void callApi() {
        int randomNum = RandomGenerator.createNumber(0, apiCallInfo.getTotalRatio());
        Class<?> selected = selectClass(randomNum);
        callMethod(selected, randomNum);
    }

    public Class<?> selectClass(int randomNum) {
        var apiInfos = apiCallInfo.getApiInfos();
        apiInfos.entrySet().stream().map(apiInfo -> apiInfo.getKey().getName()).forEach(System.out::println);
        return apiInfos.entrySet().stream()
            .filter(apiInfo -> apiInfo.getValue().get(0) <= randomNum && apiInfo.getValue().get(1) > randomNum)
            .findAny()
            .map(Map.Entry::getKey)
            .orElseThrow(() -> new IllegalArgumentException("잘못된 범위의 숫자입니다. num : " + randomNum));
    }

    private void callMethod(Class<?> selected, int randomNum) {
        Method method = getMethodName(selected);
        // 테스트용 필드
        var methodsClass = method.getDeclaringClass();
        var apiInfo = apiCallInfo.getApiInfos().get(selected);
        try {
            // 테스트용 출력코드
            System.out.println(String.format("""  
                    ======================================
                     난수 : %d,
                     선택된 클래스 : %s,
                     해당 클래스의 범위 : %d ~ %d,
                     선택된 메서드 : %s,
                     메서드가 속한 클래스 : %s
                     ======================================
                    """,
                randomNum,
                selected.getName(),
                apiInfo.get(0), apiInfo.get(1),
                method.getName(),
                methodsClass.getName()
            ));
            method.invoke(context.getBean(selected));
            System.out.println("호출 성공!");
        } catch (Exception e) {
            throw new RuntimeException("Api 호출에 실패하였습니다. message: " + e.getMessage());
        }
    }

    public Method getMethodName(Class<?> apiClass) {
        String methodName = apiClass.getAnnotation(ApiCallAnnotation.class).methodToCall();
        try {
            return apiClass.getMethod(methodName);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("존재하지 않는 메서드 입니다. method : " + methodName);
        }
    }

    public ApiCallManager of(String name, ApiCallManagerRegistry apiCallManagerRegistry, ApplicationContext context) {
        return new ApiCallManager(apiCallManagerRegistry.findByName(name), context);
    }
}

