package in.koreatech.koin.repository;

import in.koreatech.koin.domain.user.User;
import java.util.Optional;
import org.springframework.data.repository.Repository;

public interface UserRepository extends Repository<User, Long> {

    User save(User user);

    Optional<User> findByEmail(String email);
}
