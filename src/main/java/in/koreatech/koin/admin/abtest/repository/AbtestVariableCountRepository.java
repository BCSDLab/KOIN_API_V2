package in.koreatech.koin.admin.abtest.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import in.koreatech.koin.admin.abtest.model.redis.AbtestVariableCount;
import in.koreatech.koin.config.repository.RedisRepository;

@RedisRepository
public interface AbtestVariableCountRepository extends CrudRepository<AbtestVariableCount, Integer> {

    List<AbtestVariableCount> findAll();

    AbtestVariableCount save(AbtestVariableCount abtestVariableCount);

    Optional<AbtestVariableCount> findById(Integer id);

    default AbtestVariableCount findOrCreateIfNotExists(Integer id) {
        return findById(id).orElseGet(() ->
            save(AbtestVariableCount.builder()
                .variableId(id)
                .count(0)
                .build())
        );
    }
}
