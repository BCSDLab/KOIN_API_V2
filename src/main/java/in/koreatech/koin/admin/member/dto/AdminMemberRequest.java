package in.koreatech.koin.admin.member.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.member.model.Member;
import in.koreatech.koin.domain.member.model.Track;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@JsonNaming(value = SnakeCaseStrategy.class)
public record AdminMemberRequest(
    @NotNull(message = "이름은 필수입니다.")
    @Size(max = 50, message = "이름의 길이는 최대 50자입니다.")
    @Schema(description = "이름", example = "김주원", requiredMode = REQUIRED)
    String name,

    @Size(max = 10, message = "학번의 길이는 최대 10자입니다.")
    @Schema(description = "소속 트랙", example = "2019136037")
    String studentNumber,

    @NotNull(message = "트랙은 필수입니다.")
    @Pattern(regexp = "^(Android|BackEnd|FrontEnd|Game|UI\\/UX|HR|iOS|P&M)$", message = "트랙의 형식이 올바르지 않습니다.")
    @Schema(description = "고유 id", example = "BackEnd", requiredMode = REQUIRED)
    String track,

    @NotNull(message = "직급은 필수입니다.")
    @Pattern(regexp = "^(Mentor|Regular)$", message = "직급의 형식이 올바르지 않습니다.")
    @Schema(description = "직급", example = "Regular", requiredMode = REQUIRED)
    String position,

    @Email(message = "이메일의 길이는 최대 100자입니다.")
    @Schema(description = "이메일", example = "damiano102777@naver.com")
    String email,

    @Size(max = 65535, message = "이미지 링크의 길이는 최대 65535자입니다.")
    @Schema(description = "이미지 링크", example = "https://example.com")
    String imageUrl
) {
    public Member toMember(Track track) {
        return Member.builder()
            .name(name)
            .studentNumber(studentNumber)
            .track(track)
            .position(position)
            .email(email)
            .imageUrl(imageUrl)
            .build();
    }
}
