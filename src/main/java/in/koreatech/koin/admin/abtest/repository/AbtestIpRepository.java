package in.koreatech.koin.admin.abtest.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.admin.abtest.model.AbtestIp;

public interface AbtestIpRepository extends Repository<AbtestIp, Integer> {

    Optional<AbtestIp> findById(Integer id);

    // TODO: 예외 생성
    default AbtestIp getById(Integer id) {
        // return findById(id).orElseThrow(() ->
        //     )
    }
}
