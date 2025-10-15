package in.koreatech.koin.domain.student.repository;

import in.koreatech.koin.domain.student.model.redis.UnAuthenticatedStudentInfo;
import org.springframework.data.repository.Repository;

import java.util.Optional;

public interface StudentRedisRepository extends Repository<UnAuthenticatedStudentInfo, String> {

    UnAuthenticatedStudentInfo save(UnAuthenticatedStudentInfo unauthenticatedStudentInfo);

    Optional<UnAuthenticatedStudentInfo> findById(String email);

    Optional<UnAuthenticatedStudentInfo> findByNickname(String nickname);

    Optional<UnAuthenticatedStudentInfo> findByAuthToken(String authToken);

    void deleteById(String email);
}
