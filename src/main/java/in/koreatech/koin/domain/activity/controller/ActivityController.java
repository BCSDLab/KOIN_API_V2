package in.koreatech.koin.domain.activity.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.activity.dto.ActivitiesResponseDTO;
import in.koreatech.koin.domain.activity.service.ActivityService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ActivityController implements ActivityApi {

    private final ActivityService activityService;

    @GetMapping("/activities")
    public ResponseEntity<ActivitiesResponseDTO> getActivities(
        @RequestParam(required = false) String year
    ) {
        ActivitiesResponseDTO response = activityService.getActivities(year);
        return ResponseEntity.ok(response);
    }

}
