package in.koreatech.koin.domain.activity.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
public class ActivitiesResponseDTO {

    @JsonProperty("Activities")
    private List<ActivityResponse> activities;

    public ActivitiesResponseDTO(List<ActivityResponse> activities) {
        this.activities = activities;
    }
}
