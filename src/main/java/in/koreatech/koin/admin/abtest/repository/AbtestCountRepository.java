package in.koreatech.koin.admin.abtest.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import in.koreatech.koin.admin.abtest.model.AbtestCount;

public interface AbtestCountRepository extends CrudRepository<AbtestCount, Integer> {

    List<AbtestCount> findAll();

    AbtestCount save(AbtestCount abtestCount);

    // 사용 메서드 기록용 주석. 쿼리 최적화를 위해 사용. 실제 재정의 시 에러 발생
    // List<AbtestCount> saveAll(List<AbtestCount> abtestCounts);
}
