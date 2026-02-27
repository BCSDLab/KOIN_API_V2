package in.koreatech.koin.domain.dept.model;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import lombok.Getter;

@Getter
public enum Dept {
    ARCHITECTURAL_ENGINEERING(
        "건축공학부", List.of("72"),
        "https://www.koreatech.ac.kr/menu.es?mid=b30202000000"),
    EMPLOYMENT_SERVICE_POLICY_DEPARTMENT(
        "고용서비스정책학과", List.of("85"),
        "https://www.koreatech.ac.kr/board.es?mid=b80302000000&bid=0150"),
    MECHANICAL_ENGINEERING(
        "기계공학부", List.of("20"),
        "https://www.koreatech.ac.kr/menu.es?mid=a70302000000"),
    DESIGN_ENGINEERING(
        "디자인공학부", List.of("51"),
        "https://www.koreatech.ac.kr/menu.es?mid=b20302000000"),
    MECHATRONICS_ENGINEERING(
        "메카트로닉스공학부", List.of("40"),
        "https://www.koreatech.ac.kr/menu.es?mid=a80302010200"),
    INDUSTRIAL_MANAGEMENT(
        "산업경영학부", List.of("80"),
        "https://www.koreatech.ac.kr/menu.es?mid=b60302010100"),
    ELECTRICAL_AND_ELECTRONIC_COMMUNICATION_ENGINEERING(
        "전기전자통신공학부", List.of("61"),
        "https://www.koreatech.ac.kr/menu.es?mid=a90302010200"),
    COMPUTER_SCIENCE(
        "컴퓨터공학부", List.of("35", "36"),
        "https://www.koreatech.ac.kr/menu.es?mid=b10402000000"),

    // 신설 과는 학과 고유 번호로 N/A를 넣어줌
    CHEMIACL_ENGINEERING(
        "화학생명공학부", List.of("N/A"),
        "https://www.koreatech.ac.kr/board.es?mid=b50301000000&bid=0135"),
    ENERGY_MATERIALS_ENGINEERING(
        "에너지신소재공학부", List.of("N/A"),
        "https://www.koreatech.ac.kr/board.es?mid=b40301000000&bid=0128"),
    SEMICONDUCTOR_DISPLAY_ENGINEERING(
        "반도체·디스플레이공학과", List.of("N/A"),
        "https://www.koreatech.ac.kr/menu.es?mid=a10201000000"),
    FUTURE_CONVERGENCE(
        "미래융합학부", List.of("N/A"),
        "https://www.koreatech.ac.kr/menu.es?mid=a10201000000"),
    ENGINEERING_CONVERGENCE_AUTONOMOUS_MAJOR(
        "공학융합자율전공", List.of("N/A"),
        "https://www.koreatech.ac.kr/menu.es?mid=a10201000000"),
    ICT_CONVERGENCE_AUTONOMOUS_MAJOR(
        "ICT융합자율전공", List.of("N/A"),
        "https://www.koreatech.ac.kr/menu.es?mid=a10201000000"),
    SOCIAL_CONVERGENCE_AUTONOMOUS_MAJOR(
        "사회융합자율전공", List.of("N/A"),
        "https://www.koreatech.ac.kr/menu.es?mid=a10201000000"),

    // 없어진 과(고 학번을 위해 유지)
    NEW_ENERGY_MATERIALS_CHEMICAL_ENGINEERING(
        "에너지신소재화학공학부", List.of("74"),
        "https://cms3.koreatech.ac.kr/ace/992/subview.do");

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
