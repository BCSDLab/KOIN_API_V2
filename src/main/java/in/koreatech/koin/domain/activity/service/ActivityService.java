package in.koreatech.koin.domain.activity.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import in.koreatech.koin.domain.activity.dto.ActivityResponse;
import in.koreatech.koin.domain.activity.model.Activity;
import in.koreatech.koin.domain.activity.repository.ActivityRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityRepository activityRepository;

    public Map<String, List<ActivityResponse>> getActivities(String year) {
        List<Activity> activities;

        // String으로 되어 있는 imageURLs를 리스트로 바꿔주기 위한 과정
        // year parameter 값이 없는 경우 모든 값을 반환
        if (year == null) {
            activities = activityRepository.findAllByIsDeleted(false);
        } else { // parameter가 있는 경우 해당 년도 값만 반환
            activities = activityRepository.getActivitiesByYear(year);
        }

        return imageUrlStringToList(activities);
    }

    private Map<String, List<ActivityResponse>> imageUrlStringToList(List<Activity> activities) {
        List<ActivityResponse> activityResponseList = new ArrayList<>();

        for (Activity activity : activities) {
            List<String> imageUrlsList = parseImageUrls(activity.getImageUrls());
            activityResponseList.add(ActivityResponse.from(activity, imageUrlsList));
        }

        return Collections.singletonMap("Activities", activityResponseList);
    }

    private List<String> parseImageUrls(String imageUrls) {
        if (imageUrls == null || imageUrls.trim().isEmpty()) {
            return Collections.emptyList();
        }
        // 이미지 URL 분리 및 개행 문자 제거
        return Arrays.stream(imageUrls.split(","))
            .map(String::trim) // 앞뒤 공백 제거
            .map(url -> url.replace("\n", "").replace("\r", "")) // 개행 문자 제거
            .collect(Collectors.toList());
    }
}
