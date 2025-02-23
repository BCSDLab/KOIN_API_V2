package in.koreatech.koin.batch.campus.bus.city.model;

import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Builder;
import lombok.Getter;

@Getter
public class BusInfo {

    @Field("number")
    private final Long number;

    @Field("depart_node")
    private final String departNode;

    @Field("arrival_node")
    private final String arrivalNode;

    @Builder
    private BusInfo(Long number, String departNode, String arrivalNode) {
        this.number = number;
        this.departNode = departNode;
        this.arrivalNode = arrivalNode;
    }
}
