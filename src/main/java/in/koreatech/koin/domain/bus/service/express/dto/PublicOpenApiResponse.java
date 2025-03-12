package in.koreatech.koin.domain.bus.service.express.dto;

import java.util.List;

import in.koreatech.koin.domain.bus.service.express.model.PublicOpenApiExpressBusArrival;

public record PublicOpenApiResponse(
    InnerResponse response
) {
    public record InnerResponse (
        InnerHeader header,
        InnerBody body
    ){

    }

    public record InnerHeader(
        String resultCode,
        String resultMsg
    ){

    }

    public record InnerBody (
        InnerItems items,
        Integer totalCount
    ){

    }

    public record InnerItems (
        List<PublicOpenApiExpressBusArrival> item
    ) {

    }
}
