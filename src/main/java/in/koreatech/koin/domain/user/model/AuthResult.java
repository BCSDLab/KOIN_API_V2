package in.koreatech.koin.domain.user.model;

import java.util.Date;
import java.util.Optional;

import in.koreatech.koin.domain.user.dto.AuthResponse;
import lombok.Getter;

@Getter
public class AuthResult {

    private boolean isSuccess;
    private String errorMessage;

    public AuthResult(boolean isSuccess, String errorMessage) {
        this.isSuccess = isSuccess;
        this.errorMessage = errorMessage;
    }

    public static AuthResult from(Optional<User> user) {
        if (user == null) {
            return new AuthResult(false, "토큰에 해당하는 사용자를 찾을 수 없습니다.");
        }
        if (user.get().getIsAuthed() != null && user.get().getIsAuthed().equals(true)) {
            return new AuthResult(false, "이미 인증된 사용자입니다.");
        }
        if (user.get().getResetExpiredAt() != null && (user.get().getAuthExpiredAt().compareTo((new Date().toString()))
            < 0)) {
            return new AuthResult(false, "이미 만료된 토큰입니다.");
        }
        return new AuthResult(true, null);
    }

    public AuthResponse toAuthResponse() {
        return new AuthResponse(isSuccess, errorMessage);
    }
}
