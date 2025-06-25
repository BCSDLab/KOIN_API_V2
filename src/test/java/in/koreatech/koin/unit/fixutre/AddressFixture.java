package in.koreatech.koin.unit.fixutre;

import in.koreatech.koin.domain.order.address.model.OffCampusDeliveryAddress;

public class AddressFixture {

    private AddressFixture() {}

    public static OffCampusDeliveryAddress 교외_배달_가능_지역() {
        return OffCampusDeliveryAddress.builder()
            .zipNumber("31253")
            .siDo("충청남도")
            .siGunGu("천안시 동남구")
            .eupMyeonDong("병천면")
            .road("충절로")
            .buildingName(null)
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
            .buildingName("경복궁")
            .detailAddress("경복궁")
            .fullAddress("서울특별시 종로구 사직로 161 (세종로) 경복궁")
            .build();
    }
}
