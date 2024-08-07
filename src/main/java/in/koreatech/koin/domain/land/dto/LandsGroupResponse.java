package in.koreatech.koin.domain.land.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LandsGroupResponse(
    @JsonProperty("lands")
    List<LandsResponse> lands
) {

}
