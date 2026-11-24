package in.koreatech.koin.domain.language.service;

import java.util.Map;

import in.koreatech.koin.domain.language.model.SupportedLanguage;

/** 공식 명칭이 확인된 언어만 제공하며, 미등록 명칭은 원문을 보존한다. */
public final class OfficialNameResolver {

    // 학교 국·영문 캠퍼스맵의 동일 건물 번호로 대조(2026-11-09).
    // https://www.koreatech.ac.kr/campusMap/list.es?campus_id=1&mid=a10107010000
    // https://www.koreatech.ac.kr/campusMap/list.es?campus_id=1&mid=a20104010000
    private static final Map<String, String> BUILDINGS_EN = Map.ofEntries(
        Map.entry("학생통합지원센터", "Student Service Building"),
        Map.entry("복지관", "Welfare Building"),
        Map.entry("대학본부", "Administration Building"),
        Map.entry("인문경영관", "Humanities and Management Building"),
        Map.entry("국제교육센터", "Global Education Center"),
        Map.entry("다산정보관", "Dasan Information Hall(Library)"),
        Map.entry("학생회관", "Student Union Building"),
        Map.entry("공학1관", "Engineering Building 1"),
        Map.entry("공학2관", "Engineering Building 2"),
        Map.entry("공학3관", "Engineering Building 3"),
        Map.entry("공학4관A동", "Engineering Building 4(A)"),
        Map.entry("공학4관B동", "Engineering Building 4(B)"),
        Map.entry("강당", "Auditorium"),
        Map.entry("담헌실학관", "Damheon Hall of Practical Education"),
        Map.entry("KOREATECHR&D돔", "KOREATECH R&D Dome"),
        Map.entry("운동장", "Playing Field"),
        Map.entry("체육관", "Gymnasium"),
        Map.entry("해울관", "Haeul"),
        Map.entry("예지관", "Yeji"),
        Map.entry("예솔관", "Yesol"),
        Map.entry("다솔관", "Dasol"),
        Map.entry("생활관관리동", "Dormitory Office"),
        Map.entry("함지관", "Hamji"),
        Map.entry("한울관", "Hanul"),
        Map.entry("청솔관", "Cheongsol"),
        Map.entry("IH관", "International House"),
        Map.entry("참빛관", "Chambit"),
        Map.entry("은솔관", "Eunsol"),
        Map.entry("새롬관", "Saerom-Hall"),
        Map.entry("소울관", "Soul"),
        Map.entry("나우리인성관", "Personality Development Center"),
        Map.entry("창업보육관", "Business Incubator"),
        Map.entry("캠퍼스컴퍼니", "Campus Company"),
        Map.entry("산학협력관", "Industry-University Cooperation Foundation"),
        Map.entry("스마트팩토리", "SmartRunningFactory"),
        Map.entry("테니스장", "Tennis Court"),
        Map.entry("공동직장어린이집", "Joint Workplace Daycare"),
        Map.entry("솔빛관", "Dormitory (Solbit)"),
        Map.entry("다담미래학습관", "Dadam Future Learning Center"),
        Map.entry("101동(해울관)", "Haeul"),
        Map.entry("102동(예지관)", "Yeji"),
        Map.entry("103동(예솔관)", "Yesol"),
        Map.entry("104동(다솔관)", "Dasol"),
        Map.entry("105동(함지관)", "Hamji"),
        Map.entry("106동(한울관)", "Hanul"),
        Map.entry("202동(청솔관)", "Cheongsol"),
        Map.entry("203동(IH관)", "International House"),
        Map.entry("205동(참빛관)", "Chambit"),
        Map.entry("204동(은솔관)", "Eunsol"),
        Map.entry("201동(솔빛관)", "Dormitory (Solbit)"),
        Map.entry("101동(해울)", "Haeul"),
        Map.entry("102동(예지)", "Yeji"),
        Map.entry("103동(예솔)", "Yesol"),
        Map.entry("104동(다솔)", "Dasol"),
        Map.entry("105동(함지)", "Hamji"),
        Map.entry("106동(한울)", "Hanul"),
        Map.entry("202동(청솔)", "Cheongsol"),
        Map.entry("203동(IH)", "International House"),
        Map.entry("205동(참빛)", "Chambit"),
        Map.entry("204동(은솔)", "Eunsol"),
        Map.entry("201동(솔빛)", "Dormitory (Solbit)")
    );

    // 학교 공식 교통 안내. 특정 승차 위치를 역/터미널 전체 명칭으로 축약하지 않는다.
    // https://www.koreatech.ac.kr/menu.es?mid=a20105010000
    private static final Map<String, String> STATIONS_EN = Map.of(
        "천안역", "Cheonan Station",
        "천안 터미널", "Bus Terminal",
        "천안터미널", "Bus Terminal",
        "터미널", "Bus Terminal",
        "코리아텍", "KOREATECH",
        "한기대", "KOREATECH"
    );

    private OfficialNameResolver() {
    }

    public static String shopName(String original, SupportedLanguage language) {
        return original == null ? null : ContentMessageCatalog.officialName(language, "shop." + original, original);
    }

    public static String lectureName(String original, SupportedLanguage language) {
        return original == null ? null : ContentMessageCatalog.officialName(language, "lecture." + original, original);
    }

    public static String buildingName(String original, SupportedLanguage language) {
        if (original == null || language != SupportedLanguage.ENGLISH) {
            return original;
        }
        return BUILDINGS_EN.getOrDefault(original.replace(" ", ""), original);
    }

    public static String stationName(String original, SupportedLanguage language) {
        if (original == null || language != SupportedLanguage.ENGLISH) {
            return original;
        }
        return STATIONS_EN.getOrDefault(original, original);
    }
}
