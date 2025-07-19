package in.koreatech.koin.admin.owner.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.owner.model.OwnerAttachment;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record AdminOwnerResponse(
    @Schema(description = "번호", example = "1", requiredMode = REQUIRED)
    Integer id,

    @Schema(description = "이메일", example = "example@gmail.com", requiredMode = REQUIRED)
    String email,

    @Schema(description = "이름", example = "김철수", requiredMode = NOT_REQUIRED)
    String name,

    @Schema(description = "닉네임", example = "bbo", requiredMode = NOT_REQUIRED)
    String nickname,

    @Schema(description = "사업자 등록 번호", example = "012-34-56789", requiredMode = REQUIRED)
    String companyRegistrationNumber,

    @Schema(description = "첨부파일 url 목록", requiredMode = NOT_REQUIRED)
    List<String> attachmentsUrl,

    @Schema(description = "가게 id 목록", requiredMode = NOT_REQUIRED)
    List<Integer> shopsId,

    @Schema(description = "휴대폰 번호", example = "01012341234", requiredMode = NOT_REQUIRED)
    String phoneNumber,

    @Schema(description = "인증 여부", example = "true", requiredMode = REQUIRED)
    Boolean isAuthed,

    @Schema(description = "유저 타입", example = "STUDENT", requiredMode = REQUIRED)
    String userType,

    @Schema(description = "성별", example = "1", requiredMode = NOT_REQUIRED)
    Integer gender,

    @Schema(description = "created_at", example = "2018-09-02 21:48:14", requiredMode = REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdAt,

    @Schema(description = "updated_at", example = "2024-03-03 14:24:40", requiredMode = REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime updatedAt,

    @Schema(description = "last_logged_at", example = "2018-09-03 06:50:28", requiredMode = NOT_REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime lastLoggedAt
) {
    public static AdminOwnerResponse of(Owner owner, List<Integer> shopsId) {
        return new AdminOwnerResponse(
            owner.getUser().getId(),
            owner.getUser().getEmail(),
            owner.getUser().getName(),
            owner.getUser().getNickname(),
            owner.getCompanyRegistrationNumber(),
            owner.getAttachments()
                .stream()
                .map(OwnerAttachment::getUrl)
                .collect(Collectors.toList()),
            shopsId,
            owner.getAccount(),
            owner.getUser().isAuthed(),
            owner.getUser().getUserType().getValue(),
            owner.getUser().getGender() == null ? null : owner.getUser().getGender().ordinal(),
            owner.getUser().getCreatedAt(),
            owner.getUser().getUpdatedAt(),
            owner.getUser().getLastLoggedAt()
        );
    }
}
