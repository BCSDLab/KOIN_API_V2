package in.koreatech.koin.global.domain.callcontol;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import in.koreatech.koin.global.domain.callcontol.model.BaseApi;
import in.koreatech.koin.global.domain.callcontol.model.SubApi;
import in.koreatech.koin.global.domain.random.model.RandomGenerator;

@Component
public class CallControl {

    private final ApplicationContext context;
    private BaseApi baseApi;

    public CallControl(ApplicationContext context) {
        this.context = context;
    }

    public void callApi(String baseApiName) {
        baseApi = (BaseApi)context.getBean(baseApiName);
        int randomNum = RandomGenerator.createNumber(0, baseApi.totalRatio());
        SubApi selectedApi = selectApi(randomNum);
        selectedApi.callMethod();
    }

    private SubApi selectApi(int randomNum) {
        return baseApi.subApis().stream()
            .filter(apiRange -> apiRange.start() <= randomNum && apiRange.end() > randomNum)
            .findAny()
            .orElseThrow(() -> new IllegalArgumentException("범위를 벗어난 숫자입니다. number : " + randomNum));
    }
}

