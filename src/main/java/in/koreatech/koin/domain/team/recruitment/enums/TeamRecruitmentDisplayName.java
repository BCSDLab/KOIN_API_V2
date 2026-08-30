package in.koreatech.koin.domain.team.recruitment.enums;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 모집 카테고리와 진행 방식의 화면 표시명. keyword 검색이 표시명도 대상으로 하기 때문에 필요하다.
 * <p>
 * 표시명은 Swagger 의 enum 설명을 그대로 따른다. enum 자체에 표시명이 생기면 이 클래스는 없앨 수 있다.
 */
public final class TeamRecruitmentDisplayName {

    private static final Map<TeamRecruitmentCategory, String> CATEGORY_NAMES = Map.of(
        TeamRecruitmentCategory.CONTEST, "공모전",
        TeamRecruitmentCategory.EXTERNAL_ACTIVITY, "대외활동",
        TeamRecruitmentCategory.STUDY, "스터디",
        TeamRecruitmentCategory.PROJECT, "프로젝트",
        TeamRecruitmentCategory.OTHER, "기타"
    );

    private static final Map<TeamRecruitmentMeetingType, String> MEETING_TYPE_NAMES = Map.of(
        TeamRecruitmentMeetingType.ONLINE, "온라인",
        TeamRecruitmentMeetingType.OFFLINE, "오프라인",
        TeamRecruitmentMeetingType.MIXED, "온/오프라인"
    );

    private TeamRecruitmentDisplayName() {
    }

    public static String of(TeamRecruitmentCategory category) {
        return CATEGORY_NAMES.get(category);
    }

    public static String of(TeamRecruitmentMeetingType meetingType) {
        return MEETING_TYPE_NAMES.get(meetingType);
    }

    /**
     * 표시명에 검색어가 포함되는 카테고리를 찾는다.
     * enum 영문 이름은 대상이 아니다. 예를 들어 "test" 로 CONTEST 가 걸리면 안 된다.
     */
    public static List<TeamRecruitmentCategory> categoriesMatching(String keyword) {
        return Arrays.stream(TeamRecruitmentCategory.values())
            .filter(category -> matches(keyword, of(category)))
            .toList();
    }

    /**
     * 표시명에 검색어가 포함되는 진행 방식을 찾는다.
     * enum 영문 이름은 대상이 아니다. 예를 들어 "line" 으로 ONLINE 이 걸리면 안 된다.
     */
    public static List<TeamRecruitmentMeetingType> meetingTypesMatching(String keyword) {
        return Arrays.stream(TeamRecruitmentMeetingType.values())
            .filter(meetingType -> matches(keyword, of(meetingType)))
            .toList();
    }

    private static boolean matches(String keyword, String displayName) {
        return displayName.toLowerCase().contains(keyword.toLowerCase());
    }
}
