package in.koreatech.koin.admin.history.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import in.koreatech.koin.admin.history.dto.AdminHistoryResponse;
import in.koreatech.koin.admin.history.dto.AdminHistoriesCondition;
import in.koreatech.koin.admin.history.dto.AdminHistoriesResponse;
import in.koreatech.koin.admin.history.model.AdminActivityHistory;
import in.koreatech.koin.admin.history.repository.AdminActivityHistoryRepository;
import in.koreatech.koin.common.model.Criteria;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HistoryService {

    private final AdminActivityHistoryRepository adminActivityHistoryRepository;

    public AdminHistoriesResponse getHistories(AdminHistoriesCondition condition) {
        Integer total = adminActivityHistoryRepository.countAdminActivityHistory();
        Criteria criteria = Criteria.of(condition.page(), condition.limit(), total);

        PageRequest pageRequest = PageRequest.of(
            criteria.getPage(), criteria.getLimit(),
            Sort.by(condition.sort().getDirection(), "createdAt")
        );
        Page<AdminActivityHistory> adminActivityHistoryPage = adminActivityHistoryRepository.findByConditions(
            condition, pageRequest);

        return AdminHistoriesResponse.from(adminActivityHistoryPage);
    }

    public AdminHistoryResponse getHistory(Integer id) {
        AdminActivityHistory adminActivityHistory = adminActivityHistoryRepository.getById(id);
        return AdminHistoryResponse.from(adminActivityHistory);
    }
}
