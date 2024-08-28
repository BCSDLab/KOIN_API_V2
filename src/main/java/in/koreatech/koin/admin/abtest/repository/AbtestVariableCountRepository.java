package in.koreatech.koin.admin.abtest.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import in.koreatech.koin.admin.abtest.exception.AbtestVariableCountNotFoundException;
import in.koreatech.koin.admin.abtest.model.redis.AbtestVariableCount;

public interface AbtestVariableCountRepository extends CrudRepository<AbtestVariableCount, Integer> {

    List<AbtestVariableCount> findAll();

    AbtestVariableCount save(AbtestVariableCount abtestVariableCount);

    Optional<AbtestVariableCount> findById(Integer id);

    default AbtestVariableCount getById(Integer id) {
        return findById(id).orElseThrow(() ->
            AbtestVariableCountNotFoundException.withDetail("abtestVariableCount id: " + id));
    }

    default AbtestVariableCount findOrCreateIfNotExists(Integer id) {
        return findById(id).orElseGet(() ->
            save(AbtestVariableCount.builder()
                .variableId(id)
                .count(0)
                .build())
        );
    }

    // 사용 메서드 기록용 주석. 쿼리 최적화를 위해 사용. 실제 재정의 시 에러 발생
    // List<AbtestCount> saveAll(List<AbtestCount> abtestCounts);
}
