package in.koreatech.koin.domain.user.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.user.domain.User;

public interface UserRepository extends Repository<User, Long> {

    User save(User user);

    Optional<User> findByEmail(String email);

    Optional<User> findById(Long id);
}
