package in.koreatech.koin.domain.team.recruitment.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@JsonNaming(SnakeCaseStrategy.class)
public record TeamRecruitmentProfileUpsertRequest(
    @Schema(description = "팀원 모집 전용 닉네임", example = "홍길동", requiredMode = REQUIRED)
    @NotBlank
    @Size(min = 1, max = 20)
    String profileNickname,

    @Schema(description = "선호 역할", example = "기획", requiredMode = REQUIRED)
    @NotBlank
    @Size(min = 1, max = 20)
    String preferredRole,

    @Schema(description = "보유 기술. 기존 목록을 전부 대체합니다.", example = "[\"정보처리기사\"]", requiredMode = REQUIRED)
    @NotNull
    List<@NotBlank @Size(min = 1, max = 20) String> skills,

    @Schema(description = "활동 내역. 기존 목록을 전부 대체하며 각 항목에 id를 보내지 않습니다.", requiredMode = REQUIRED)
    @NotNull
    List<@NotNull @Valid ProfileActivityInput> activities,

    @Schema(description = "자기소개", example = "안녕하세요.", requiredMode = REQUIRED)
    @NotBlank
    @Size(min = 1, max = 1000)
    String selfIntroduction
) {
}
