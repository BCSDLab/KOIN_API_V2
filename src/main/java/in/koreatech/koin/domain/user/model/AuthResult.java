package in.koreatech.koin.domain.user.model;

import java.util.Date;
import java.util.Optional;

import in.koreatech.koin.domain.user.dto.AuthResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(staticName = "of")
public class AuthResult {

    private final boolean isSuccess;
    private final String errorMessage;


    public static AuthResult USER_NOT_FOUND = AuthResult.of(false, "토큰에 해당하는 사용자를 찾을 수 없습니다.");
    public static AuthResult ALREADY_AUTHENTICATION = AuthResult.of(false, "이미 인증된 사용자입니다.");
    public static AuthResult TOKEN_EXPIRED = AuthResult.of(false, "이미 만료된 토큰입니다.");
    public static AuthResult SUCCESS = AuthResult.of(true , null);

    public static AuthResult from(Optional<User> user) {
        if(user == null) {
            return USER_NOT_FOUND;
        }
        if(user.get().getIsAuthed() != null && user.get().getIsAuthed().equals(true)) {
            return ALREADY_AUTHENTICATION;
        }
        if(user.get().getResetExpiredAt() != null && (user.get().getAuthExpiredAt().compareTo((new Date().toString()))<0)) {
            return TOKEN_EXPIRED;
        }
        return SUCCESS;
    }

    public AuthResponse toAuthResponse(){
        return new AuthResponse(isSuccess, errorMessage);
    }
}
