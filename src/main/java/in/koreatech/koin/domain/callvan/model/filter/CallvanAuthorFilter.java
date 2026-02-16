package in.koreatech.koin.domain.callvan.model.filter;

import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CallvanAuthorFilter {
    ALL {
        @Override
        public Integer getRequiredAuthorId(Integer userId) {
            return null;
        }
    },
    MY {
        @Override
        public Integer getRequiredAuthorId(Integer userId) {
            if (userId == null) {
                throw CustomException.of(ApiResponseCode.UNAUTHORIZED_USER);
            }
            return userId;
        }
    };

    public abstract Integer getRequiredAuthorId(Integer userId);
}
