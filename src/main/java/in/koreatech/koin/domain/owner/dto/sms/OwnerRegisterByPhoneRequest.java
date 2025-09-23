package in.koreatech.koin.domain.owner.dto.sms;

import static in.koreatech.koin.domain.user.model.UserType.OWNER;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.validator.constraints.URL;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.owner.model.OwnerAttachment;
import in.koreatech.koin.domain.user.model.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@JsonNaming(SnakeCaseStrategy.class)
public record OwnerRegisterByPhoneRequest(

    @Pattern(regexp = "^\\d{3}-\\d{2}-\\d{5}", message = "사업자 등록 번호 형식이 올바르지 않습니다. ${validatedValue}")
    @Schema(description = "사업자 등록 번호", example = "012-34-56789", requiredMode = NOT_REQUIRED)
    String companyNumber,

    @NotBlank(message = "이름은 필수입니다.")
    @Size(max = 50, message = "이름은 50자 이내여야 합니다.")
    @Schema(description = "이름", example = "최준호", requiredMode = REQUIRED)
    String name,

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Schema(description = "비밀번호", example = "password", requiredMode = REQUIRED)
    String password,

    @NotBlank(message = "휴대폰 번호는 필수입니다.")
    @Schema(description = "휴대폰 번호", example = "01000000000", requiredMode = REQUIRED)
    @Pattern(regexp = "^\\d{11}$", message = "전화번호 형식이 올바르지 않습니다. 11자리 숫자로 입력해 주세요.")
    String phoneNumber,

    @Schema(description = "상점 고유 ID", requiredMode = NOT_REQUIRED)
    Integer shopId,

    @NotBlank(message = "상점 이름은 필수입니다.")
    @Schema(description = "상점 이름", example = "고릴라밥", requiredMode = REQUIRED)
    String shopName,

    @NotBlank(message = "상점 전화번호는 필수입니다.")
    @Schema(description = "상점 전화번호", example = "0415605555 또는 01012345678", requiredMode = REQUIRED)
    @Pattern(regexp = "^\\d{10,11}$", message = "가게 전화번호 형식이 올바르지 않습니다. 10~11자리 숫자로 입력해 주세요.")
    String shopNumber,

    @Size(min = 1, max = 5, message = "이미지는 사업자등록증, 영업신고증, 통장사본을 포함하여 최소 1개 최대 5개까지 가능합니다.")
    @Schema(description = "첨부 이미지들", requiredMode = REQUIRED)
    @Valid
    List<OwnerRegisterByPhoneInnerAttachmentUrl> attachmentUrls
) {

    public Owner toOwner(PasswordEncoder passwordEncoder) {
        User user = User.builder()
            .loginPw(passwordEncoder.encode(password))
            .name(name)
            .email(null)
            .userType(OWNER)
            .isAuthed(false)
            .isDeleted(false)
            .build();

        Owner owner = Owner.builder()
            .user(user)
            .companyRegistrationNumber(companyNumber)
            .attachments(new ArrayList<>())
            .grantShop(false)
            .grantEvent(false)
            .account(phoneNumber)
            .build();

        List<OwnerAttachment> attachments = attachmentUrls.stream()
            .map(OwnerRegisterByPhoneInnerAttachmentUrl::fileUrl)
            .map(fileUrl -> OwnerAttachment.builder()
                .url(fileUrl)
                .owner(owner)
                .isDeleted(false)
                .name(name)
                .build())
            .toList();

        owner.getAttachments().addAll(attachments);
        return owner;
    }

    @JsonNaming(SnakeCaseStrategy.class)
    public record OwnerRegisterByPhoneInnerAttachmentUrl(
        @NotBlank(message = "첨부 파일 URL은 필수입니다.")
        @URL(protocol = "https", regexp = ".*static\\.koreatech\\.in.*", message = "코인 파일 저장 형식이 아닙니다.")
        @Schema(description = "첨부 파일 URL (코인 파일 형식이어야 함)", example = "https://static.koreatech.in/1.png", requiredMode = REQUIRED)
        String fileUrl
    ) {

    }
}
