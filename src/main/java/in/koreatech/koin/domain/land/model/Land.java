package in.koreatech.koin.domain.land.model;

import static lombok.AccessLevel.PROTECTED;

import java.util.List;
import java.util.stream.Collectors;

import in.koreatech.koin._common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "lands")
@NoArgsConstructor(access = PROTECTED)
public class Land extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Size(max = 255)
    @NotNull
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Size(max = 50)
    @NotNull
    @Column(name = "internal_name", nullable = false, length = 50)
    private String internalName;

    @Column(name = "size", length = 20)
    private String size;

    @Size(max = 20)
    @Column(name = "room_type", length = 20)
    private String roomType;

    @Column(name = "latitude", length = 20)
    private String latitude;

    @Column(name = "longitude", length = 20)
    private String longitude;

    @Size(max = 20)
    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "image_urls")
    private String imageUrls;

    @Column(name = "address")
    private String address;

    @Column(name = "description")
    private String description;

    @Column(name = "floor")
    private Integer floor;

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
    private boolean optRefrigerator = false;

    @NotNull
    @Column(name = "opt_closet", nullable = false)
    private boolean optCloset = false;

    @NotNull
    @Column(name = "opt_tv", nullable = false)
    private boolean optTv = false;

    @NotNull
    @Column(name = "opt_microwave", nullable = false)
    private boolean optMicrowave = false;

    @NotNull
    @Column(name = "opt_gas_range", nullable = false)
    private boolean optGasRange = false;

    @NotNull
    @Column(name = "opt_induction", nullable = false)
    private boolean optInduction = false;

    @NotNull
    @Column(name = "opt_water_purifier", nullable = false)
    private boolean optWaterPurifier = false;

    @NotNull
    @Column(name = "opt_air_conditioner", nullable = false)
    private boolean optAirConditioner = false;

    @NotNull
    @Column(name = "opt_washer", nullable = false)
    private boolean optWasher = false;

    @NotNull
    @Column(name = "opt_bed", nullable = false)
    private boolean optBed = false;

    @NotNull
    @Column(name = "opt_desk", nullable = false)
    private boolean optDesk = false;

    @NotNull
    @Column(name = "opt_shoe_closet", nullable = false)
    private boolean optShoeCloset = false;

    @NotNull
    @Column(name = "opt_electronic_door_locks", nullable = false)
    private boolean optElectronicDoorLocks = false;

    @NotNull
    @Column(name = "opt_bidet", nullable = false)
    private boolean optBidet = false;

    @NotNull
    @Column(name = "opt_veranda", nullable = false)
    private boolean optVeranda = false;

    @NotNull
    @Column(name = "opt_elevator", nullable = false)
    private boolean optElevator = false;

    @NotNull
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Builder
    private Land(
        String internalName,
        String name,
        String size,
        String roomType,
        String latitude,
        String longitude,
        String phone,
        List<String> imageUrls,
        String address,
        String description,
        Integer floor,
        String deposit,
        String monthlyFee,
        String charterFee,
        String managementFee,
        boolean optRefrigerator,
        boolean optCloset,
        boolean optTv,
        boolean optMicrowave,
        boolean optGasRange,
        boolean optInduction,
        boolean optWaterPurifier,
        boolean optAirConditioner,
        boolean optWasher,
        boolean optBed,
        boolean optDesk,
        boolean optShoeCloset,
        boolean optElectronicDoorLocks,
        boolean optBidet,
        boolean optVeranda,
        boolean optElevator,
        boolean isDeleted
    ) {
        this.internalName = internalName;
        this.name = name;
        this.size = size;
        this.roomType = roomType;
        this.latitude = latitude;
        this.longitude = longitude;
        this.phone = phone;
        this.imageUrls = convertToSting(imageUrls);
        this.address = address;
        this.description = description;
        this.floor = floor;
        this.deposit = deposit;
        this.monthlyFee = monthlyFee;
        this.charterFee = charterFee;
        this.managementFee = managementFee;
        this.optRefrigerator = optRefrigerator;
        this.optCloset = optCloset;
        this.optTv = optTv;
        this.optMicrowave = optMicrowave;
        this.optGasRange = optGasRange;
        this.optInduction = optInduction;
        this.optWaterPurifier = optWaterPurifier;
        this.optAirConditioner = optAirConditioner;
        this.optWasher = optWasher;
        this.optBed = optBed;
        this.optDesk = optDesk;
        this.optShoeCloset = optShoeCloset;
        this.optElectronicDoorLocks = optElectronicDoorLocks;
        this.optBidet = optBidet;
        this.optVeranda = optVeranda;
        this.optElevator = optElevator;
        this.isDeleted = isDeleted;
    }

    public Double getLatitude() {
        if (this.latitude == null) {
            return null;
        }
        return Double.parseDouble(latitude);
    }

    public Double getLongitude() {
        if (this.longitude == null) {
            return null;
        }
        return Double.parseDouble(longitude);
    }

    public Double getSize() {
        if (this.size == null) {
            return null;
        }
        return Double.parseDouble(size);
    }

    private String convertToSting(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return null;
        }
        return String.format("[%s]", imageUrls.stream()
            .map(url -> "\"" + url + "\"")
            .collect(Collectors.joining(",")));
    }

    public void delete() {
        this.isDeleted = true;
    }

    public void undelete() {
        this.isDeleted = false;
    }

    public void update(String internalName, String name, double size, String roomType, double latitude,
        double longitude,
        String phone, List<String> imageUrls, String address, String description, Integer floor,
        String deposit, String monthlyFee, String charterFee, String managementFee, boolean optRefrigerator,
        boolean optCloset, boolean optTv, boolean optMicrowave, boolean optGasRange, boolean optInduction,
        boolean optWaterPurifier, boolean optAirConditioner, boolean optWasher, boolean optBed, boolean optDesk,
        boolean optShoeCloset, boolean optElectronicDoorLocks, boolean optBidet, boolean optVeranda,
        boolean optElevator) {
        this.internalName = internalName;
        this.name = name;
        this.size = String.valueOf(size);
        this.roomType = roomType;
        this.latitude = String.valueOf(latitude);
        this.longitude = String.valueOf(longitude);
        this.phone = phone;
        this.imageUrls = convertToSting(imageUrls);
        this.address = address;
        this.description = description;
        this.floor = floor;
        this.deposit = deposit;
        this.monthlyFee = monthlyFee;
        this.charterFee = charterFee;
        this.managementFee = managementFee;
        this.optRefrigerator = optRefrigerator;
        this.optCloset = optCloset;
        this.optTv = optTv;
        this.optMicrowave = optMicrowave;
        this.optGasRange = optGasRange;
        this.optInduction = optInduction;
        this.optWaterPurifier = optWaterPurifier;
        this.optAirConditioner = optAirConditioner;
        this.optWasher = optWasher;
        this.optBed = optBed;
        this.optDesk = optDesk;
        this.optShoeCloset = optShoeCloset;
        this.optElectronicDoorLocks = optElectronicDoorLocks;
        this.optBidet = optBidet;
        this.optVeranda = optVeranda;
        this.optElevator = optElevator;
    }
}
