package in.koreatech.koin.domain.dept.model;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import lombok.Getter;

@Getter
public enum Dept {
    ARCHITECTURAL_ENGINEERING(
        "건축공학부", List.of("72"),
        "https://cms3.koreatech.ac.kr/arch/1083/subview.do"),
    EMPLOYMENT_SERVICE_POLICY_DEPARTMENT(
        "고용서비스정책학과", List.of("85"),
        "https://www.koreatech.ac.kr/kor/CMS/UnivOrganMgr/subMain.do?mCode=MN451"),
    MECHANICAL_ENGINEERING(
        "기계공학부", List.of("20"),
        "https://cms3.koreatech.ac.kr/me/795/subview.do"),
    DESIGN_ENGINEERING(
        "디자인공학부", List.of("51"),
        "https://cms3.koreatech.ac.kr/ide/1047/subview.do"),
    MECHATRONICS_ENGINEERING(
        "메카트로닉스공학부", List.of("40"),
        "https://www.koreatech.ac.kr/kor/CMS/UnivOrganMgr/subMain.do?mCode=MN076"),
    INDUSTRIAL_MANAGEMENT(
        "산업경영학부", List.of("80"),
        "https://cms3.koreatech.ac.kr/sim/1167/subview.do"),
    NEW_ENERGY_MATERIALS_CHEMICAL_ENGINEERING(
        "에너지신소재화학공학부", List.of("74"),
        "https://cms3.koreatech.ac.kr/ace/992/subview.do"),
    ELECTRICAL_AND_ELECTRONIC_COMMUNICATION_ENGINEERING(
        "전기전자통신공학부", List.of("61"),
        "https://cms3.koreatech.ac.kr/ite/842/subview.do"),
    COMPUTER_SCIENCE(
        "컴퓨터공학부", List.of("35", "36"),
        "https://cse.koreatech.ac.kr/page_izgw21"),
    ;

    private final String name;
    private final List<String> numbers;
    private final String curriculumLink;

    Dept(String name, List<String> numbers, String curriculumLink) {
        this.name = name;
        this.numbers = numbers;
        this.curriculumLink = curriculumLink;
    }

    public static Optional<Dept> findByNumber(String number) {
        for (Dept dept : Dept.values()) {
            if (dept.numbers.contains(number)) {
                return Optional.of(dept);
            }
        }
        return Optional.empty();
    }

    public static List<Dept> findAll() {
        return Arrays.stream(values()).toList();
    }
}
