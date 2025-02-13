package in.koreatech.koin.domain.bus.service.shuttle.model;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonNaming(value = SnakeCaseStrategy.class)
public class ArrivalNode {

    private String nodeName;

    private String arrivalTime;

    @Builder
    private ArrivalNode(String nodeName, String arrivalTime) {
        this.nodeName = nodeName;
        this.arrivalTime = arrivalTime;
    }
}
