package in.koreatech.koin.domain.activity.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
public class ActivitiesResponse {

    @JsonProperty("Activities")
    private List<ActivityResponse> activities;

    public ActivitiesResponse(List<ActivityResponse> activities) {
        this.activities = activities;
    }
}
