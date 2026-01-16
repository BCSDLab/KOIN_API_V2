package in.koreatech.koin.domain.community.article.model.filter;

import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LostItemAuthorFilter {

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
