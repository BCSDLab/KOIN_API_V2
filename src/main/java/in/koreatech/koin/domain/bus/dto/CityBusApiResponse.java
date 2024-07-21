package in.koreatech.koin.domain.bus.dto;

import java.util.List;
import java.util.Optional;

import in.koreatech.koin.domain.bus.model.city.CityBusArrival;
import lombok.Builder;

public record CityBusApiResponse(
    InnerResponse response
) {
    public record InnerResponse(
        InnerHeader header,
        InnerBody body
    ) {
        @Builder
        public InnerResponse (InnerHeader header, InnerBody body) {
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
        List<CityBusArrival> item
    ) {
        @Builder
        public InnerItems(List<CityBusArrival> item) {
            this.item = item;
        }
    }

    @Builder
    public CityBusApiResponse(InnerResponse response) {
        this.response = response;
    }
}
