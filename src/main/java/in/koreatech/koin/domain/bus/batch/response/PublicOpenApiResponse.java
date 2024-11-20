package in.koreatech.koin.domain.bus.batch.response;

import java.util.List;

public record PublicOpenApiResponse(
    InnerResponse response
) {
    public record InnerResponse(
        InnerHeader header,
        InnerBody body
    ) {

    }

    public record InnerHeader(
        String resultCode,
        String resultMsg
    ) {

    }

    public record InnerBody(
        InnerItems items,
        Integer totalCount
    ) {

    }

    public record InnerItems(
        List<PublicOpenApiExpressBusArrival> item
    ) {

    }
}
