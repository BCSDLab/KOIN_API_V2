package in.koreatech.koin.global.domain.callcontoller;

import org.springframework.stereotype.Component;

import in.koreatech.koin.global.domain.callcontoller.model.ParentApi;
import in.koreatech.koin.global.domain.callcontoller.model.ChildApi;
import in.koreatech.koin.global.domain.random.model.RandomGenerator;
import in.koreatech.koin.global.model.ReflectionUtils;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CallController {

    public void callApi(String beanName) {
        ParentApi parentApi = (ParentApi)ReflectionUtils.findBeanByName(beanName);
        int randomNum = RandomGenerator.createNumber(0, parentApi.totalRatio());
        ChildApi selectedApi = selectChildApi(parentApi, randomNum);
        selectedApi.callMethod();
    }

    private ChildApi selectChildApi(ParentApi parentApi, int randomNum) {
        return parentApi.childApis().stream()
            .filter(apiRange -> apiRange.getStart() <= randomNum && apiRange.getEnd() > randomNum)
            .findAny()
            .orElseThrow(() -> new IllegalArgumentException("범위를 벗어난 숫자입니다. number : " + randomNum));
    }
}

