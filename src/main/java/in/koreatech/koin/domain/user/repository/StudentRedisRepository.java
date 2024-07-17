package in.koreatech.koin.domain.user.repository;

import in.koreatech.koin.domain.user.model.redis.StudentTemporaryStatus;
import org.springframework.data.repository.Repository;

import java.util.Optional;

public interface StudentRedisRepository extends Repository<StudentTemporaryStatus, String> {

    StudentTemporaryStatus save(StudentTemporaryStatus studentTemporaryStatus);

    Optional<StudentTemporaryStatus> findById(String email);

    Optional<StudentTemporaryStatus> findByNickname(String nickname);

    Optional<StudentTemporaryStatus> findByAuthToken(String authToken);

    void deleteById(String email);
}
