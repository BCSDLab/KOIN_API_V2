package in.koreatech.koin.domain.activity.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ActivitiesResponse(
    @JsonProperty("Activities")
    List<ActivityResponse> activities
) {
    public ActivitiesResponse(List<ActivityResponse> activities) {
        this.activities = activities;
    }
}
