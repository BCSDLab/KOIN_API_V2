package in.koreatech.koin.domain.bus.dto;

import java.util.List;

import in.koreatech.koin.domain.bus.model.city.CityBusRoute;
import lombok.Builder;

public record CityBusRouteApiResponse(
    InnerResponse response
) {
    public record InnerResponse(
        InnerHeader header,
        InnerBody body
    ) {
        @Builder
        public InnerResponse(InnerHeader header, InnerBody body) {
            this.header = header;
            this.body = body;
        }
    }

    public record InnerHeader(
        String resultCode,
        String resultMsg
    ) {
        @Builder
        public InnerHeader(String resultCode, String resultMsg) {
            this.resultCode = resultCode;
            this.resultMsg = resultMsg;
        }
    }

    public record InnerBody(
        InnerItems items,
        Integer totalCount
    ) {
        @Builder
        public InnerBody(InnerItems items, Integer totalCount) {
            this.items = items;
            this.totalCount = totalCount;
        }
    }

    public record InnerItems(
        List<CityBusRoute> item
    ) {
        @Builder
        public InnerItems(List<CityBusRoute> item) {
            this.item = item;
        }
    }

    @Builder
    public CityBusRouteApiResponse(InnerResponse response) {
        this.response = response;
    }
}
