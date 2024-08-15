package in.koreatech.koin.domain.land.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QLand is a Querydsl query type for Land
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QLand extends EntityPathBase<Land> {

    private static final long serialVersionUID = 1141655991L;

    public static final QLand land = new QLand("land");

    public final in.koreatech.koin.global.domain.QBaseEntity _super = new in.koreatech.koin.global.domain.QBaseEntity(this);

    public final StringPath address = createString("address");

    public final StringPath charterFee = createString("charterFee");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath deposit = createString("deposit");

    public final StringPath description = createString("description");

    public final NumberPath<Integer> floor = createNumber("floor", Integer.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath imageUrls = createString("imageUrls");

    public final StringPath internalName = createString("internalName");

    public final BooleanPath isDeleted = createBoolean("isDeleted");

    public final StringPath latitude = createString("latitude");

    public final StringPath longitude = createString("longitude");

    public final StringPath managementFee = createString("managementFee");

    public final StringPath monthlyFee = createString("monthlyFee");

    public final StringPath name = createString("name");

    public final BooleanPath optAirConditioner = createBoolean("optAirConditioner");

    public final BooleanPath optBed = createBoolean("optBed");

    public final BooleanPath optBidet = createBoolean("optBidet");

    public final BooleanPath optCloset = createBoolean("optCloset");

    public final BooleanPath optDesk = createBoolean("optDesk");

    public final BooleanPath optElectronicDoorLocks = createBoolean("optElectronicDoorLocks");

    public final BooleanPath optElevator = createBoolean("optElevator");

    public final BooleanPath optGasRange = createBoolean("optGasRange");

    public final BooleanPath optInduction = createBoolean("optInduction");

    public final BooleanPath optMicrowave = createBoolean("optMicrowave");

    public final BooleanPath optRefrigerator = createBoolean("optRefrigerator");

    public final BooleanPath optShoeCloset = createBoolean("optShoeCloset");

    public final BooleanPath optTv = createBoolean("optTv");

    public final BooleanPath optVeranda = createBoolean("optVeranda");

    public final BooleanPath optWasher = createBoolean("optWasher");

    public final BooleanPath optWaterPurifier = createBoolean("optWaterPurifier");

    public final StringPath phone = createString("phone");

    public final StringPath roomType = createString("roomType");

    public final StringPath size = createString("size");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QLand(String variable) {
        super(Land.class, forVariable(variable));
    }

    public QLand(Path<? extends Land> path) {
        super(path.getType(), path.getMetadata());
    }

    public QLand(PathMetadata metadata) {
        super(Land.class, metadata);
    }

}

