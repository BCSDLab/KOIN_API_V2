package in.koreatech.koin.global.domain.callcontol.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

import in.koreatech.koin.global.domain.callcontol.exception.BeanNotFoundException;
import lombok.Builder;

@Builder
public record BaseApi(
    List<SubApi> subApis,
    int totalRatio
) {

    public static BaseApi of(Class<?> baseApiType, ApplicationContext context) {
        int total = 0;
        List<SubApi> subApis = new ArrayList<>();
        List<?> subApiTypes = getSubApiTypes(baseApiType, context);
        for (var subApiType : subApiTypes) {
            SubApi subApi = SubApi.of(subApiType, total);
            total += subApi.end();
            subApis.add(subApi);
        }
        return BaseApi.builder()
            .subApis(subApis)
            .totalRatio(total)
            .build();
    }

    private static List<?> getSubApiTypes(Class<?> BaseApiType, ApplicationContext context) {
        try {
            return context.getBeansOfType(BaseApiType).values().stream().toList();
        } catch (NoSuchBeanDefinitionException e) {
            throw new BeanNotFoundException("하위 타입이 존재하지 않습니다. 상위 타입: " + BaseApiType);
        }
    }
}
