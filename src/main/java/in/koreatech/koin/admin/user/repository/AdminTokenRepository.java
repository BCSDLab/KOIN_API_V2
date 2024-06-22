package in.koreatech.koin.admin.user.repository;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.user.model.UserToken;

public interface AdminTokenRepository extends Repository<UserToken, Integer> {

    UserToken save(UserToken userToken);
}
