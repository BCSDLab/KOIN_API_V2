package in.koreatech.koin.domain.order.address.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.order.address.model.RoadNameAddressDocument;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;

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
        String roadAddress,
        @Schema(description = "지번 주소")
        String jibunAddress,
        @Schema(description = "영문 주소")
        String engAddress,
        @Schema(description = "우편번호")
        String zipNo,
        @Schema(description = "건물명")
        String bdNm,
        @Schema(description = "시도명")
        String siNm,
        @Schema(description = "시군구명")
        String sggNm,
        @Schema(description = "읍면동명")
        String emdNm,
        @Schema(description = "법정리명")
        String liNm,
        @Schema(description = "도로명")
        String rn

    ) {

        public static AddressInfo from(RoadNameAddressApiResponse.Juso juso) {
            return new AddressInfo(
                juso.roadAddr(),
                juso.jibunAddr(),
                juso.engAddr(),
                juso.zipNo(),
                juso.bdNm(),
                juso.siNm(),
                juso.sggNm(),
                juso.emdNm(),
                juso.liNm(),
                juso.rn()
            );
        }

        public static AddressInfo from(RoadNameAddressDocument document) {
            return new AddressInfo(
                document.getRoadAddress(),
                document.getJibunAddress(),
                document.getEngAddress(),
                document.getZipNo(),
                document.getBdNm(),
                document.getSiNm(),
                document.getSggNm(),
                document.getEmdNm(),
                document.getLiNm(),
                document.getRn()
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

    public static AddressSearchResponse from(Page<RoadNameAddressDocument> page) {
        List<AddressInfo> addressInfos = page.getContent().stream()
            .map(AddressInfo::from)
            .collect(Collectors.toList());

        return new AddressSearchResponse(
            String.valueOf(page.getTotalElements()),
            page.getNumber() + 1,
            page.getSize(),
            addressInfos
        );
    }
}
