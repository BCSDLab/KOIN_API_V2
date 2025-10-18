package in.koreatech.koin.domain.order.address.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;

@Builder
public record RoadNameAddressApiResponse(
    Results results
) {

    @Builder
    public record Results(
        Common common,
        @JsonProperty("juso")
        List<Juso> jusoList
    ) {

    }

    @Builder
    public record Common(
        String totalCount,
        Integer currentPage,
        Integer countPerPage,
        String errorCode,
        String errorMessage
    ) {

    }

    @Builder
    public record Juso(
        String roadAddr,
        String roadAddrPart1,
        String roadAddrPart2,
        String jibunAddr,
        String engAddr,
        String zipNo,
        String admCd,
        String rnMgtSn,
        String bdMgtSn,
        String detBdNmList,
        String bdNm,
        String bdKdcd,
        String siNm,
        String sggNm,
        String emdNm,
        String liNm,
        String rn,
        String udrtYn,
        String buldMnnm,
        String buldSlnm,
        String mtYn,
        String lnbrMnnm,
        String lnbrSlnm,
        String emdNo,
        String hstryYn,
        String relJibun,
        String hemdNm
    ) {

    }
}
