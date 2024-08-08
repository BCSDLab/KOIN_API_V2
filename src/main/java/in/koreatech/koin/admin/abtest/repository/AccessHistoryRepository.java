package in.koreatech.koin.admin.abtest.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.admin.abtest.model.AccessHistory;

public interface AccessHistoryRepository extends Repository<AccessHistory, Integer> {

    AccessHistory save(AccessHistory accessHistory);

    Optional<AccessHistory> findByPublicIp(String publicIp);
}
