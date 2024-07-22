package in.koreatech.koin.global.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.bus.util.ExpressBusClient;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CallApiController {

    private final ApplicationContext applicationContext;
    private final Map<ExpressBusClient, List<Integer>> apiList = new HashMap<>();
    private int totalRatio = 0;

    public void controlApiCall(Class<ExpressBusClient> busApi, int num) {
        Map<String, ExpressBusClient> busApiList = getApiList(busApi);
        matchApiRatio(busApiList);
        callApi(num);
    }

    public Map<String, ExpressBusClient> getApiList(Class<ExpressBusClient> busApi) {
        try {
            return applicationContext.getBeansOfType(busApi);
        } catch (NoSuchBeanDefinitionException e) {
            throw new IllegalArgumentException("해당 타입이 존재하지 않습니다. 타입: " + busApi);
        }
    }

    public void matchApiRatio(Map<String, ExpressBusClient> busApiList) {
        busApiList
            .forEach((name, bean) -> {
                List<Integer> ratioList = new ArrayList<>();
                ratioList.add(totalRatio);
                Class<?> apiClass = AopProxyUtils.ultimateTargetClass(bean);
                totalRatio += apiClass.getAnnotation(CallRatio.class).ratio();
                ratioList.add(totalRatio);
                apiList.put(bean, ratioList);
            });
    }

    public void callApi(int num) {
        for (var api : apiList.entrySet()) {
            List<Integer> range = api.getValue();
            if (range.get(0) <= num && range.get(1) > num) {
                api.getKey().storeRemainTimeByOpenApi();
                return;
            }
        }
    }
}
