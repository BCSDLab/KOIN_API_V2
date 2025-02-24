package in.koreatech.koin.domain.graduation.enums;

import java.util.Map;

import in.koreatech.koin.global.exception.KoinIllegalArgumentException;
import lombok.Getter;

@Getter
public enum GeneralEducationAreaEnum {
    // key : 이수구분, value : 이수학점
    YEAR_2019("2019", Map.of(
        "글로벌", 2, "예술과문학", 3, "사회와심리", 3, "역사와철학", 3, "자연과인간", 3
    )),
    YEAR_2020("2020", Map.of(
        "글로벌", 2, "예술과문학", 3, "사회와심리", 3, "역사와철학", 3, "자연과인간", 3
    )),
    YEAR_2021("2021", Map.of(
        "인성과소양", 2, "예술과문학", 3, "사회와심리", 3, "역사와철학", 3, "자연과인간", 3
    , "미래와융합", 3)),
    YEAR_2022("2022", Map.of(
        "인성과소양", 2, "예술과문학", 3, "사회와심리", 3, "역사와철학", 3, "자연과인간", 3
        , "미래와융합", 3)),
    YEAR_2023("2023", Map.of(
        "인성과소양", 2, "예술과문학", 3, "사회와심리", 3, "역사와철학", 3, "자연과인간", 3
        , "미래와융합", 3)),
    YEAR_2024("2024", Map.of(
        "인성과소양", 2, "예술과문학", 3, "사회와심리", 3, "역사와철학", 3, "자연과인간", 3
        , "미래와융합", 3));

    private final String year;
    private final Map<String, Integer> areasWithCredits; // 교양 영역과 필요 학점 매핑

    GeneralEducationAreaEnum(String year, Map<String, Integer> areasWithCredits) {
        this.year = year;
        this.areasWithCredits = areasWithCredits;
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
