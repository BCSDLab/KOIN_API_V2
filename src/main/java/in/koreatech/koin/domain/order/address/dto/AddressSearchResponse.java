package in.koreatech.koin.domain.order.address.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@JsonNaming(value = SnakeCaseStrategy.class)
public record AddressSearchResponse(

    @Schema(description = "전체 검색 데이터 수")
    String totalCount,

    @Schema(description = "현재 페이지 번호")
    Integer currentPage,

    @Schema(description = "페이지당 결과 수")
    Integer countPerPage,

    @Schema(description = "주소 목록")
    List<AddressInfo> addresses
) {

    @JsonNaming(value = SnakeCaseStrategy.class)
    public record AddressInfo(
        @Schema(description = "전체 도로명 주소")
        String roadAddr,

        @Schema(description = "지번 주소")
        String jibunAddr,

        @Schema(description = "영문 주소")
        String engAddr,

        @Schema(description = "우편번호")
        String zipNo
    ) {

        public static AddressInfo from(RoadNameAddressApiResponse.Juso juso) {
            return new AddressInfo(
                juso.roadAddr(),
                juso.jibunAddr(),
                juso.engAddr(),
                juso.zipNo()
            );
        }
    }

    public static AddressSearchResponse from(RoadNameAddressApiResponse apiResponse) {
        RoadNameAddressApiResponse.Results results = apiResponse.results();
        RoadNameAddressApiResponse.Common common = results.common();
        List<AddressInfo> addressInfos;

        if (results.jusoList() != null && !results.jusoList().isEmpty()) {
            addressInfos = results.jusoList().stream()
                .map(AddressInfo::from)
                .collect(Collectors.toList());
        } else {
            addressInfos = Collections.emptyList();
        }

        return new AddressSearchResponse(
            common.totalCount(),
            common.currentPage(),
            common.countPerPage(),
            addressInfos
        );
    }
}
