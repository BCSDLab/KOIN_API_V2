package in.koreatech.koin.domain.graduation.enums;

import java.util.Arrays;
import java.util.Map;

import in.koreatech.koin.global.exception.custom.KoinIllegalArgumentException;
import lombok.Getter;

@Getter
public enum GeneralEducationAreaEnum {
    // key : 이수구분, value : 이수학점
    YEAR_2019("2019", Map.of(
        "글로벌", 2, "예술과문학", 3, "사회와심리", 3, "역사와철학", 3, "자연과인간", 3)),
    YEAR_2020("2020", Map.of(
        "글로벌", 2, "예술과문학", 3, "사회와심리", 3, "역사와철학", 3, "자연과인간", 3)),
    YEAR_2021("2021", Map.of(
        "인성과소양", 2, "예술과문학", 3, "사회와심리", 3, "역사와철학", 3, "자연과인간", 3)),
    YEAR_2022("2022", Map.of(
        "인성과소양", 2, "예술과문학", 3, "사회와심리", 3, "역사와철학", 3, "자연과인간", 3)),
    YEAR_2023("2023", Map.of(
        "인성과소양", 2, "예술과문학", 3, "사회와심리", 3, "역사와철학", 3, "자연과인간", 3)),
    YEAR_2024("2024", Map.of(
        "인성과소양", 2, "예술과문학", 3, "사회와심리", 3, "역사와철학", 3, "자연과인간", 3));

    private final String year;
    private final Map<String, Integer> areasWithCredits;

    GeneralEducationAreaEnum(String year, Map<String, Integer> areasWithCredits) {
        this.year = year;
        this.areasWithCredits = areasWithCredits;
    }

    public static GeneralEducationAreaEnum fromYear(String year) {
        return Arrays.stream(GeneralEducationAreaEnum.values())
            .filter(area -> area.year.equals(year))
            .findFirst()
            .orElseThrow(() -> new KoinIllegalArgumentException("year 양식이 잘못됐습니다. year : " + year));
    }
}
