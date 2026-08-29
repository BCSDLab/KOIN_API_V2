package in.koreatech.koin.domain.team.recruitment.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record ProfileSnapshot(
    @Schema(description = "지원 당시 닉네임", example = "김철수", requiredMode = REQUIRED)
    String nickname,

    @Schema(description = "지원 당시 학과", example = "컴퓨터공학부", requiredMode = REQUIRED)
    String department,

    @Schema(description = "지원 당시 입학 연도", example = "2023", requiredMode = REQUIRED)
    Integer studentYear,

    @Schema(description = "지원 당시 희망 역할", example = "프론트엔드", requiredMode = REQUIRED)
    String preferredRole,

    @Schema(description = "지원 당시 기술", requiredMode = REQUIRED)
    List<String> skills,

    @Schema(description = "지원 당시 활동", requiredMode = REQUIRED)
    List<ProfileActivity> activities,

    @Schema(description = "지원 당시 자기소개", example = "안녕하세요.", requiredMode = REQUIRED)
    String selfIntroduction
) {
}
