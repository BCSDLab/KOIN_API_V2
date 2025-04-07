package in.koreatech.koin.domain.user.dto;

import in.koreatech.koin._common.validation.EmailOrPhone;

public record SendVerificationCodeRequest(
    @EmailOrPhone
    String target
) {

}
