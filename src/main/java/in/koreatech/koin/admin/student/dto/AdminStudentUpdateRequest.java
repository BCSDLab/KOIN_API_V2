package in.koreatech.koin.admin.student.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@JsonNaming(value = SnakeCaseStrategy.class)
public record AdminStudentUpdateRequest(
    @Schema(description = "성별(남:0, 여:1)", example = "1", requiredMode = NOT_REQUIRED)
    Integer gender,

    @Schema(description = "[NOT UPDATE]신원(학생, 사장님)", example = "학생", requiredMode = NOT_REQUIRED)
    Integer identity,

    @Schema(description = "[NOT UPDATE]졸업 여부(true, false)", example = "false", requiredMode = NOT_REQUIRED)
    Boolean isGraduated,

    @Schema(description = """
        전공
        - 기계공학부
        - 컴퓨터공학부
        - 메카트로닉스공학부
        - 전기전자통신공학부
        - 디자인공학부
        - 건축공학부
        - 화학생명공학부
        - 에너지신소재공학부
        - 산업경영학부
        - 고용서비스정책학부
        """, example = "컴퓨터공학부", requiredMode = NOT_REQUIRED)
    String major,

    @Size(max = 50, message = "이름의 길이는 최대 50자 입니다.")
    @Schema(description = "이름", example = "최준호", requiredMode = NOT_REQUIRED)
    String name,

    @Size(message = "비밀번호 (SHA 256 해싱된 값)")
    @Schema(description = "비밀번호", example = "a0240120305812krlakdsflsa;1235", requiredMode = NOT_REQUIRED)
    String password,

    @Size(max = 10, message = "닉네임은 10자 이내여야 합니다.")
    @Schema(description = "닉네임", example = "juno", requiredMode = NOT_REQUIRED)
    String nickname,

    @Schema(description = "휴대폰 번호", example = "010-0000-0000", requiredMode = NOT_REQUIRED)
    @Size(max = 20, message = "휴대전화의 길이는 최대 20자 입니다")
    String phoneNumber,

    @Size(min = 10, max = 10, message = "학번은 10자여야 합니다.")
    @Schema(description = "학번", example = "2020136065", requiredMode = NOT_REQUIRED)
    String studentNumber
) {

}
