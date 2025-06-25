package in.koreatech.koin.domain.order.address.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OffCampusDeliveryAddress {

    @Column(name = "zip_number")
    private Integer zipNumber;

    @Column(name = "si_do", length = 50)
    private String siDo;

    @Column(name = "si_gun_gu", length = 50)
    private String siGunGu;

    @Column(name = "eup_myeon_dong", length = 50)
    private String eupMyeonDong;

    @Column(name = "li", length = 50)
    private String li;

    @Column(name = "road", length = 50)
    private String road;

    @Column(name = "building_name", length = 50)
    private String buildingName;

    @Column(name = "detail_address", length = 100)
    private String detailAddress;

    @Column(name = "full_address", length = 255)
    private String fullAddress;

    @Builder
    public OffCampusDeliveryAddress(Integer zipNumber, String siDo, String siGunGu, String eupMyeonDong, String li, String road,
        String buildingName, String detailAddress, String fullAddress) {
        this.zipNumber = zipNumber;
        this.siDo = siDo;
        this.siGunGu = siGunGu;
        this.eupMyeonDong = eupMyeonDong;
        this.li = li;
        this.road = road;
        this.buildingName = buildingName;
        this.detailAddress = detailAddress;
        this.fullAddress = fullAddress;
    }

    public Boolean isValidDeliveryArea(String siDo, String siGunGu, String eupMyeonDong) {
        return siDo.equals(this.siDo) &&
            siGunGu.equals(this.siGunGu) &&
            eupMyeonDong.equals(this.eupMyeonDong);
    }
}
