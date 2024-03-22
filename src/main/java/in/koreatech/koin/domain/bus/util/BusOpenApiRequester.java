package in.koreatech.koin.domain.bus.util;

import java.util.List;

import in.koreatech.koin.domain.bus.model.Bus;

public abstract class BusOpenApiRequester<T extends Bus> {

    public abstract List<T> getBusRemainTime(String nodeId);

}
