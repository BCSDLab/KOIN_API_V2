package in.koreatech.koin.domain.graduation.enums;

import java.util.List;

import in.koreatech.koin.global.exception.KoinIllegalArgumentException;
import lombok.Getter;

@Getter
public enum GeneralEducationAreaEnum {
    YEAR_2019("2019", "글로벌", List.of("예술과문학", "사회와심리", "역사와철학", "자연과인간")),
    YEAR_2020("2020", "글로벌", List.of("예술과문학", "사회와심리", "역사와철학", "자연과인간")),
    YEAR_2021("2021", "인성과소양", List.of("예술과문학", "사회와심리", "역사와철학", "자연과인간")),
    YEAR_2022("2022", "인성과소양", List.of("예술과문학", "사회와심리", "역사와철학", "자연과인간")),
    YEAR_2023("2023", "인성과소양", List.of("예술과문학", "사회와심리", "역사와철학", "자연과인간")),
    YEAR_2024("2024", "인성과소양", List.of("예술과문학", "사회와심리", "역사와철학", "자연과인간"));

    private final String year;
    private final String creditArea; // 이수학점 계산이 필요한 일반 교양 영역 내 예외 영역
    private final List<String> areas; // 일반 교양 영역

    GeneralEducationAreaEnum(String year, String creditArea, List<String> areas) {
        this.year = year;
        this.creditArea = creditArea;
        this.areas = areas;
    }

    public static GeneralEducationAreaEnum fromYear(String year) {
        for (GeneralEducationAreaEnum generalEducationArea : GeneralEducationAreaEnum.values()) {
            if (generalEducationArea.year.equals(year)) {
                return generalEducationArea;
            }
        }
        throw new KoinIllegalArgumentException("year 양식이 잘못됐습니다. year : " + year);
    }
}
