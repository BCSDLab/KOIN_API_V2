package in.koreatech.koin.domain.student.repository;

import in.koreatech.koin.domain.student.model.redis.UnauthenticatedStudentInfo;
import org.springframework.data.repository.Repository;

import java.util.Optional;

public interface StudentRedisRepository extends Repository<UnauthenticatedStudentInfo, String> {

    UnauthenticatedStudentInfo save(UnauthenticatedStudentInfo unauthenticatedStudentInfo);

    Optional<UnauthenticatedStudentInfo> findById(String email);

    Optional<UnauthenticatedStudentInfo> findByNickname(String nickname);

    Optional<UnauthenticatedStudentInfo> findByAuthToken(String authToken);

    void deleteById(String email);
}
