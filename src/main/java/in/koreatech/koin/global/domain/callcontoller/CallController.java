package in.koreatech.koin.global.domain.callcontoller;

import java.util.List;

import org.springframework.stereotype.Component;

import in.koreatech.koin.global.domain.random.model.RandomGenerator;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CallController {

    public <T> T selectCallApi(List<T> children) {
        CallApiList<T> callApiList = new CallApiList<>(children);
        int randomNum = RandomGenerator.createNumber(0, callApiList.getCallApiList().size());
        return callApiList.getCallApiList().get(randomNum);
    }
}

