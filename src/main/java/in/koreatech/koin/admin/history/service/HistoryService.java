package in.koreatech.koin.admin.history.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import in.koreatech.koin.admin.history.dto.AdminHistoryResponse;
import in.koreatech.koin.admin.history.dto.AdminHistorysCondition;
import in.koreatech.koin.admin.history.dto.AdminHistorysResponse;
import in.koreatech.koin.admin.history.model.AdminActivityHistory;
import in.koreatech.koin.admin.history.repository.AdminActivityHistoryRepository;
import in.koreatech.koin.global.model.Criteria;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HistoryService {

    private final AdminActivityHistoryRepository adminActivityHistoryRepository;

    public AdminHistorysResponse getHistorys(AdminHistorysCondition condition) {
        Integer total = adminActivityHistoryRepository.countAdminActivityHistory();
        Criteria criteria = Criteria.of(condition.page(), condition.limit(), total);

        PageRequest pageRequest = PageRequest.of(criteria.getPage(), criteria.getLimit());
        Page<AdminActivityHistory> adminActivityHistoryRepositoryPage = adminActivityHistoryRepository.findByConditions(
            condition,
            pageRequest);

        return AdminHistorysResponse.of(adminActivityHistoryRepositoryPage);
    }

    public AdminHistoryResponse getHistory(Integer id) {
        AdminActivityHistory adminActivityHistory = adminActivityHistoryRepository.getById(id);
        return AdminHistoryResponse.from(adminActivityHistory);
    }
}
