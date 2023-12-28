package in.koreatech.koin.domain.land.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "lands")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Land {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "int UNSIGNED not null")
    private Long id;

    @Size(max = 255)
    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @Size(max = 50)
    @NotNull
    @Column(name = "internal_name", nullable = false, length = 50)
    private String internalName;

    @Size(max = 20)
    @Column(name = "size", length = 20)
    private String size;

    @Size(max = 20)
    @Column(name = "room_type", length = 20)
    private String roomType;

    @Size(max = 20)
    @Column(name = "latitude", length = 20)
    private String latitude;

    @Size(max = 20)
    @Column(name = "longitude", length = 20)
    private String longitude;

    @Size(max = 20)
    @Column(name = "phone", length = 20)
    private String phone;

    @Lob
    @Column(name = "image_urls")
    private String imageUrls;

    @Lob
    @Column(name = "address")
    private String address;

    @Lob
    @Column(name = "description")
    private String description;

    @Column(name = "floor", columnDefinition = "int UNSIGNED")
    private Long floor;

    @Size(max = 255)
    @Column(name = "deposit")
    private String deposit;

    @Size(max = 255)
    @Column(name = "monthly_fee")
    private String monthlyFee;

    @Size(max = 20)
    @Column(name = "charter_fee", length = 20)
    private String charterFee;

    @Size(max = 255)
    @Column(name = "management_fee")
    private String managementFee;

    @NotNull
    @Column(name = "opt_refrigerator", nullable = false)
    private Boolean optRefrigerator = false;

    @NotNull
    @Column(name = "opt_closet", nullable = false)
    private Boolean optCloset = false;

    @NotNull
    @Column(name = "opt_tv", nullable = false)
    private Boolean optTv = false;

    @NotNull
    @Column(name = "opt_microwave", nullable = false)
    private Boolean optMicrowave = false;

    @NotNull
    @Column(name = "opt_gas_range", nullable = false)
    private Boolean optGasRange = false;

    @NotNull
    @Column(name = "opt_induction", nullable = false)
    private Boolean optInduction = false;

    @NotNull
    @Column(name = "opt_water_purifier", nullable = false)
    private Boolean optWaterPurifier = false;

    @NotNull
    @Column(name = "opt_air_conditioner", nullable = false)
    private Boolean optAirConditioner = false;

    @NotNull
    @Column(name = "opt_washer", nullable = false)
    private Boolean optWasher = false;

    @NotNull
    @Column(name = "opt_bed", nullable = false)
    private Boolean optBed = false;

    @NotNull
    @Column(name = "opt_desk", nullable = false)
    private Boolean optDesk = false;

    @NotNull
    @Column(name = "opt_shoe_closet", nullable = false)
    private Boolean optShoeCloset = false;

    @NotNull
    @Column(name = "opt_electronic_door_locks", nullable = false)
    private Boolean optElectronicDoorLocks = false;

    @NotNull
    @Column(name = "opt_bidet", nullable = false)
    private Boolean optBidet = false;

    @NotNull
    @Column(name = "opt_veranda", nullable = false)
    private Boolean optVeranda = false;

    @NotNull
    @Column(name = "opt_elevator", nullable = false)
    private Boolean optElevator = false;

    @NotNull
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Builder
    private Land(String internalName, String name, String size, String roomType, String latitude, String longitude,
        String phone, String imageUrls, String address, String description, Long floor, String deposit,
        String monthlyFee, String charterFee, String managementFee) {
        this.internalName = internalName;
        this.name = name;
        this.size = size;
        this.roomType = roomType;
        this.latitude = latitude;
        this.longitude = longitude;
        this.phone = phone;
        this.imageUrls = imageUrls;
        this.address = address;
        this.description = description;
        this.floor = floor;
        this.deposit = deposit;
        this.monthlyFee = monthlyFee;
        this.charterFee = charterFee;
        this.managementFee = managementFee;
    }
}
