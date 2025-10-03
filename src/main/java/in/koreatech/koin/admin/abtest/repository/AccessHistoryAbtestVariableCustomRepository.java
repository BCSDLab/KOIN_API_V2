package in.koreatech.koin.admin.abtest.repository;

import java.util.List;

import in.koreatech.koin.global.config.repository.JpaRepository;

@JpaRepository
public interface AccessHistoryAbtestVariableCustomRepository {

    List<Integer> findIdsToMove(Integer fromVariableId, int limit);

    void updateVariableIds(List<Integer> idsToUpdate, Integer toVariableId);
}
