package in.koreatech.koin.domain.student.repository;

import in.koreatech.koin.domain.student.model.redis.unauthenticatedStudentInfo;
import org.springframework.data.repository.Repository;

import java.util.Optional;

public interface StudentRedisRepository extends Repository<unauthenticatedStudentInfo, String> {

    unauthenticatedStudentInfo save(unauthenticatedStudentInfo unauthenticatedStudentInfo);

    Optional<unauthenticatedStudentInfo> findById(String email);

    Optional<unauthenticatedStudentInfo> findByNickname(String nickname);

    Optional<unauthenticatedStudentInfo> findByAuthToken(String authToken);

    void deleteById(String email);
}
