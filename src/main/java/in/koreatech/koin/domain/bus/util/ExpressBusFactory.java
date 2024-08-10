package in.koreatech.koin.domain.bus.util;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.bus.exception.ApiTypeNotFoundException;
import in.koreatech.koin.global.domain.callcontoller.CallControllable;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ExpressBusFactory implements CallControllable<ExpressBusClient<?, ?>> {

    private final List<ExpressBusClient<?, ?>> expressBusClients;
    private final List<ExpressBusClient<?, ?>> callApiList = new ArrayList<>();

    public <T> ExpressBusClient<?, ?> getInstance(Class<T> type) {
        return expressBusClients.stream()
            .filter(client -> type.isInstance(client))
            .findAny()
            .orElseThrow(() -> ApiTypeNotFoundException.withDetail(type.getName()));
    }

    @Override
    public ExpressBusClient<?, ?> getInstanceByRatio() {
        if (callApiList.isEmpty()) {
            callApiList.addAll(generateCallApiList(expressBusClients));
        }
        return selectCallApi(callApiList);
    }
}
