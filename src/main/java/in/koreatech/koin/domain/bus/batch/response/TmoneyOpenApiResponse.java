package in.koreatech.koin.domain.bus.batch.response;

import java.util.List;

public record TmoneyOpenApiResponse(
    String code,
    String message,
    InnerResponse response
) {
    public record InnerResponse(
        List<TmoneyOpenApiExpressBusArrival> LINE_LIST
    ) {

    }
}
