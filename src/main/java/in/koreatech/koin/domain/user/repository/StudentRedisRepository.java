package in.koreatech.koin.domain.user.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.user.model.redis.StudentTemporaryStatus;

public interface StudentRedisRepository extends Repository<StudentTemporaryStatus, String> {

    StudentTemporaryStatus save(StudentTemporaryStatus studentTemporaryStatus);

    Optional<StudentTemporaryStatus> findById(String key);

    Optional<StudentTemporaryStatus> findByAuthToken(String authToken);

    void deleteById(String key);
}
