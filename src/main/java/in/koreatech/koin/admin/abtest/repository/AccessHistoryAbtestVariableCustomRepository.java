package in.koreatech.koin.admin.abtest.repository;

import java.util.List;

public interface AccessHistoryAbtestVariableCustomRepository {

    List<Integer> findIdsToMove(Integer fromVariableId, int limit);

    void updateVariableIds(List<Integer> idsToUpdate, Integer toVariableId);
}
