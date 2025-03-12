package in.koreatech.koin.domain.bus.service.express.dto;

import java.util.List;

import in.koreatech.koin.domain.bus.service.express.model.TmoneyOpenApiExpressBusArrival;

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
