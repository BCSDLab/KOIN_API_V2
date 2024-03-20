package in.koreatech.koin.domain.activity.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.activity.dto.ActivitiesResponse;
import in.koreatech.koin.domain.activity.dto.ActivityResponse;
import in.koreatech.koin.domain.activity.model.Activity;
import in.koreatech.koin.domain.activity.repository.ActivityRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ActivityService {

    private final ActivityRepository activityRepository;

    public ActivitiesResponse getActivities(String year) {
        List<Activity> activities;

        if (year == null) {
            activities = activityRepository.findAllByIsDeleted(false);
        } else {
            activities = activityRepository.getActivitiesByYear(year);
        }

        List<ActivityResponse> activityResponseList = activities.stream()
            .map(activity -> {
                List<String> imageUrlsList = parseImageUrls(activity.getImageUrls());
                return ActivityResponse.of(activity, imageUrlsList);
            })
            .collect(Collectors.toList());

        return new ActivitiesResponse(activityResponseList);
    }

    private List<String> parseImageUrls(String imageUrls) {
        if (imageUrls == null || imageUrls.trim().isEmpty()) {
            return Collections.emptyList();
        }

        return Arrays.stream(imageUrls.split(","))
            .map(String::trim)
            .map(url -> url.replace("\n", "").replace("\r", "")) // 개행 문자 제거
            .collect(Collectors.toList());
    }
}
