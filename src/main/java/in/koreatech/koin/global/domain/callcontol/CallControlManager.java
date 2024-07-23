package in.koreatech.koin.global.domain.callcontol;

import org.springframework.stereotype.Component;

import in.koreatech.koin.global.domain.random.model.RandomGenerator;

@Component
public class CallControlManager {

    private final CallControl callControl;

    public CallControlManager(CallControl callControl) {
        this.callControl = callControl;
    }

    public void callApi() {
        int randomNum = RandomGenerator.createNumber(0, callControl.totalRatio());
        ApiInfo selectedApi = selectApi(randomNum);
        try {
            selectedApi.targetMethod().invoke(selectedApi.targetClass());
        } catch (Exception e) {
            throw new RuntimeException("Api 호출에 실패하였습니다. message: " + e.getMessage());
        }
    }

    public ApiInfo selectApi(int randomNum) {
        return callControl.apiInfos().stream()
            .filter(apiRange -> apiRange.start() <= randomNum && apiRange.end() > randomNum)
            .findAny()
            .orElseThrow(() -> new IllegalArgumentException("해당하는 범위가 없습니다. randomNum : " + randomNum));
    }
}

