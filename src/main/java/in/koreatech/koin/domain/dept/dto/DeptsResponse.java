package in.koreatech.koin.domain.dept.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.dept.model.Dept;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record DeptsResponse(
    @Schema(description = "학과 명", example = "컴퓨터공학부", requiredMode = REQUIRED)
    String name,
    @Schema(description = "커리큘럼 바로가기 링크", example = "https://www.koreatech.ac.kr/menu.es?mid=b10402000000", requiredMode = REQUIRED)
    String curriculumLink,

    @Schema(description = "학과 번호들", example = """
        [ "35", "36" ]
        """, requiredMode = REQUIRED)
    List<String> deptNums
) {

    public static DeptsResponse from(Dept dept) {
        return new DeptsResponse(
            dept.getName(),
            dept.getCurriculumLink(),
            dept.getNumbers()
        );
    }
}
