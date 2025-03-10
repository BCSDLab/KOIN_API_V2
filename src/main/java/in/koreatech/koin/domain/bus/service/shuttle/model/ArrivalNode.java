package in.koreatech.koin.domain.bus.service.shuttle.model;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonNaming(value = SnakeCaseStrategy.class)
public class ArrivalNode {

    @Field("node_name")
    private String nodeName;

    @Field("arrival_time")
    private String arrivalTime;
}
