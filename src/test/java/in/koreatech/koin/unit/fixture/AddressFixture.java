package in.koreatech.koin.unit.fixture;

import java.util.List;

import in.koreatech.koin.domain.order.address.dto.RoadNameAddressApiResponse;
import in.koreatech.koin.domain.order.delivery.model.OffCampusDeliveryAddress;

public class AddressFixture {

    private AddressFixture() {}

    public static OffCampusDeliveryAddress 교외_배달_가능_지역() {
        return OffCampusDeliveryAddress.builder()
            .zipNumber("31253")
            .siDo("충청남도")
            .siGunGu("천안시 동남구")
            .eupMyeonDong("병천면")
            .road("충절로")
            .building(null)
            .detailAddress("에듀윌 301호")
            .fullAddress("충청남도 천안시 동남구 병천면 충절로 1628-17 에듀윌 301호")
            .build();
    }

    public static OffCampusDeliveryAddress 교외_배달_불가_지역() {
        return OffCampusDeliveryAddress.builder()
            .zipNumber("03045")
            .siDo("서울특별시")
            .siGunGu("종로구")
            .eupMyeonDong("세종로")
            .road("사직로")
            .building("경복궁")
            .detailAddress("경복궁")
            .fullAddress("서울특별시 종로구 사직로 161 (세종로) 경복궁")
            .build();
    }

    public static RoadNameAddressApiResponse 주소_검색_결과_한국기술교육대학교() {
        RoadNameAddressApiResponse.Juso juso = RoadNameAddressApiResponse.Juso.builder()
            .roadAddr("충청남도 천안시 동남구 병천면 충절로 1600")
            .roadAddrPart1("충청남도 천안시 동남구 병천면 충절로 1600")
            .roadAddrPart2("")
            .jibunAddr("충청남도 천안시 동남구 병천면 가전리 307 한국기술교육대학교")
            .engAddr("1600 Chungjeol-ro, Byeongcheon-myeon, Dongnam-gu, Cheonan-si, Chungcheongnam-do")
            .zipNo("31253")
            .admCd("4413136021")
            .rnMgtSn("441313249055")
            .bdMgtSn("4413136021103070000040184")
            .detBdNmList("수위실")
            .bdNm("한국기술교육대학교")
            .bdKdcd("0")
            .siNm("충청남도")
            .sggNm("천안시 동남구")
            .emdNm("병천면")
            .liNm("가전리")
            .rn("충절로")
            .udrtYn("0")
            .buldMnnm("1600")
            .buldSlnm("0")
            .mtYn("0")
            .lnbrMnnm("307")
            .lnbrSlnm("0")
            .emdNo("08")
            .build();

        RoadNameAddressApiResponse.Common common = RoadNameAddressApiResponse.Common.builder()
            .totalCount("1")
            .currentPage(1)
            .countPerPage(10)
            .errorCode("0")
            .errorMessage("정상")
            .build();

        RoadNameAddressApiResponse.Results results = RoadNameAddressApiResponse.Results.builder()
            .common(common)
            .jusoList(List.of(juso))
            .build();

        return RoadNameAddressApiResponse.builder()
            .results(results)
            .build();
    }

    public static RoadNameAddressApiResponse 주소_검색_결과_없음() {
        RoadNameAddressApiResponse.Common common = RoadNameAddressApiResponse.Common.builder()
            .totalCount("0")
            .currentPage(1)
            .countPerPage(10)
            .errorCode("0")
            .errorMessage("정상")
            .build();

        RoadNameAddressApiResponse.Results results = RoadNameAddressApiResponse.Results.builder()
            .common(common)
            .jusoList(List.of())
            .build();

        return RoadNameAddressApiResponse.builder()
            .results(results)
            .build();
    }
}
