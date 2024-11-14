package in.koreatech.koin.domain.bus.dto.express;

import java.util.List;

import in.koreatech.koin.domain.bus.model.express.TmoneyOpenApiExpressBusArrival;

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
