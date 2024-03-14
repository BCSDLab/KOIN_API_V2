package in.koreatech.koin.domain.activity.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        List<ActivityResponse> activityResponseList = new ArrayList<ActivityResponse>();

        // string으로 들어 있는 image를 list로 반환
        for (Activity activity : activities) {
            String urls = activity.getImageUrls();
            List<String> list = new ArrayList<String>();

            // image가 두 개 이상인 경우 //
            try {
                String[] array = urls.split(",");
                List<String> imagelist = Arrays.asList(array);
                list = imagelist;
            }

            //image가 한개인 경우
            catch (Exception e) {
                list.add(urls);
            }
            activityResponseList.add(ActivityResponse.from(activity, list));
        }

        return listToMap("Activities", activityResponseList);
    }

    private Map<String, List<ActivityResponse>> listToMap(String name, List<ActivityResponse> activityResponseList) {
        Map<String, List<ActivityResponse>> actvitiesMap = new HashMap<>();
        actvitiesMap.put(name, activityResponseList);
        return actvitiesMap;
    }
}
