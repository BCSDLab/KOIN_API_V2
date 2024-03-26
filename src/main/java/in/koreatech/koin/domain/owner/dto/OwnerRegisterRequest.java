package in.koreatech.koin.domain.owner.dto;

import java.util.List;

import org.hibernate.validator.constraints.URL;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.owner.model.OwnerAttachment;
import in.koreatech.koin.domain.user.model.User;
import static in.koreatech.koin.domain.user.model.UserType.OWNER;
import io.swagger.v3.oas.annotations.media.Schema;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@JsonNaming(SnakeCaseStrategy.class)
public record OwnerRegisterRequest(

    @Pattern(regexp = "^[0-9]{3}-[0-9]{2}-[0-9]{5}", message = "사업자 등록 번호 형식이 올바르지 않습니다. ${validatedValue}")
    @Schema(description = "사업자 등록 번호", example = "012-34-56789", requiredMode = NOT_REQUIRED)
    String companyNumber,

    @Email(message = "이메일 형식이 올바르지 않습니다. ${validatedValue}")
    @NotBlank(message = "이메일은 필수입니다.")
    @Schema(description = "이메일", example = "junho5336@gmail.com", requiredMode = REQUIRED)
    String email,

    @NotBlank(message = "이름은 필수입니다.")
    @Size(max = 50, message = "이름은 50자 이내여야 합니다.")
    @Schema(description = "이름", example = "최준호", requiredMode = REQUIRED)
    String name,

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Schema(description = "비밀번호", example = "password", requiredMode = REQUIRED)
    String password,

    @Pattern(regexp = "^[0-9]{3}-[0-9]{3,4}-[0-9]{4}", message = "전화번호 형식이 올바르지 않습니다.")
    @Schema(description = "휴대폰 번호", example = "010-0000-0000", requiredMode = REQUIRED)
    String phoneNumber,

    @Schema(description = "상점 고유 ID", requiredMode = NOT_REQUIRED)
    Long shopId,

    @Schema(description = "상점 이름", example = "고릴라밥", requiredMode = NOT_REQUIRED)
    String shopName,

    @Size(min = 1, max = 5, message = "이미지는 사업자등록증, 영업신고증, 통장사본을 포함하여 최소 1개 최대 5개까지 가능합니다.")
    @Schema(description = "첨부 이미지들", requiredMode = REQUIRED)
    @Valid
    List<InnerAttachmentUrl> attachmentUrls
) {

    public Owner toOwner(PasswordEncoder passwordEncoder) {
        var user = User.builder()
            .password(passwordEncoder.encode(password))
            .email(email)
            .name(name)
            .phoneNumber(phoneNumber)
            .userType(OWNER)
            .isAuthed(false)
            .isDeleted(false)
            .build();
        var attachments = attachmentUrls.stream()
            .map(InnerAttachmentUrl::fileUrl)
            .map(fileUrl -> OwnerAttachment.builder()
                .url(fileUrl)
                .isDeleted(false)
                .name(name)
                .build())
            .toList();
        return Owner.builder()
            .user(user)
            .companyRegistrationNumber(companyNumber)
            .attachments(attachments)
            .grantShop(false)
            .grantEvent(false)
            .build();
    }

    @JsonNaming(SnakeCaseStrategy.class)
    public record InnerAttachmentUrl(
        @NotBlank(message = "첨부 파일 URL은 필수입니다.")
        @URL(protocol = "https", regexp = ".*static\\.koreatech\\.in.*", message = "코인 파일 저장 형식이 아닙니다.")
        @Schema(description = "첨부 파일 URL (코인 파일 형식이어야 함)", example = "https://static.koreatech.in/1.png")
        String fileUrl
    ) {

    }
}
