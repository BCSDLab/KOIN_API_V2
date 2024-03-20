package in.koreatech.koin.domain.land.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LandsGroupResponse {
    @JsonProperty("lands")
    private List<LandsResponse> lands;

    public LandsGroupResponse(List<LandsResponse> lands) {
        this.lands = lands;
    }
}
