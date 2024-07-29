package in.koreatech.koin.global.domain.callcontol;

import org.springframework.stereotype.Component;

import in.koreatech.koin.global.domain.callcontol.model.BaseApi;
import in.koreatech.koin.global.domain.callcontol.model.SubApi;
import in.koreatech.koin.global.domain.random.model.RandomGenerator;
import in.koreatech.koin.global.model.ReflectionUtils;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CallControl {

    private BaseApi baseApi;

    public void callApi(String beanName) {
        baseApi = (BaseApi)ReflectionUtils.findBeanByName(beanName);
        int randomNum = RandomGenerator.createNumber(0, baseApi.totalRatio());
        SubApi selectedApi = selectApi(randomNum);
        selectedApi.callMethod();
    }

    private SubApi selectApi(int randomNum) {
        return baseApi.subApis().stream()
            .filter(apiRange -> apiRange.getStart() <= randomNum && apiRange.getEnd() > randomNum)
            .findAny()
            .orElseThrow(() -> new IllegalArgumentException("범위를 벗어난 숫자입니다. number : " + randomNum));
    }
}

