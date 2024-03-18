package in.koreatech.koin.domain.activity.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.activity.dto.ActivityResponse;
import in.koreatech.koin.domain.activity.service.ActivityService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ActivityController implements ActivityApi {

    private final ActivityService activityService;

    @GetMapping("/activities")
    public ResponseEntity<Map<String, List<ActivityResponse>>> getActivities(
        @RequestParam(required = false) String year
    ) {
        var response = activityService.getActivities(year);
        return ResponseEntity.ok(response);
    }
}
