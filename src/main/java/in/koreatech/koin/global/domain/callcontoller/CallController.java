package in.koreatech.koin.global.domain.callcontoller;

import java.util.List;

import org.springframework.stereotype.Component;

import in.koreatech.koin.global.domain.random.model.RandomGenerator;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CallController {

    public <T> T selectChildClass(List<T> children) {
        CallApiList<T> parent = new CallApiList<>(children);
        int randomNum = RandomGenerator.createNumber(0, parent.getCallApiList().size());
        return parent.getCallApiList().get(randomNum);
    }
}

