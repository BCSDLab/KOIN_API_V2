package in.koreatech.koin.admin.abtest.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQueryFactory;

import in.koreatech.koin.admin.abtest.model.QAccessHistoryAbtestVariable;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class AccessHistoryAbtestVariableCustomRepositoryImpl
    implements AccessHistoryAbtestVariableCustomRepository {

    private final JPAQueryFactory queryFactory;

    public List<Integer> findIdsToMove(Integer fromVariableId, int limit) {
        return queryFactory.select(QAccessHistoryAbtestVariable.accessHistoryAbtestVariable.id)
            .from(QAccessHistoryAbtestVariable.accessHistoryAbtestVariable)
            .where(QAccessHistoryAbtestVariable.accessHistoryAbtestVariable.variable.id.eq(fromVariableId))
            .limit(limit)
            .fetch();
    }

    public void updateVariableIds(List<Integer> idsToUpdate, Integer toVariableId) {
        queryFactory.update(QAccessHistoryAbtestVariable.accessHistoryAbtestVariable)
            .set(QAccessHistoryAbtestVariable.accessHistoryAbtestVariable.variable.id, toVariableId)
            .where(QAccessHistoryAbtestVariable.accessHistoryAbtestVariable.id.in(idsToUpdate))
            .execute();
    }
}
