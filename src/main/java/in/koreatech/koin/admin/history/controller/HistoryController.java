package in.koreatech.koin.admin.history.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;
import static in.koreatech.koin._common.model.Criteria.Sort;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.admin.history.dto.AdminHistoryResponse;
import in.koreatech.koin.admin.history.dto.AdminHistoriesCondition;
import in.koreatech.koin.admin.history.dto.AdminHistoriesResponse;
import in.koreatech.koin.admin.history.enums.DomainType;
import in.koreatech.koin.admin.history.enums.HttpMethodType;
import in.koreatech.koin.admin.history.service.HistoryService;
import in.koreatech.koin._common.auth.Auth;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class HistoryController implements HistoryApi {

    private final HistoryService historyService;

    @GetMapping("/admin/histories")
    public ResponseEntity<AdminHistoriesResponse> getHistories(
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer limit,
        @RequestParam(required = false) HttpMethodType requestMethod,
        @RequestParam(required = false) DomainType domainName,
        @RequestParam(required = false) Integer domainId,
        @RequestParam(required = false) Sort sort,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        AdminHistoriesCondition adminHistoriesCondition = new AdminHistoriesCondition(page, limit, requestMethod,
            domainName, domainId, sort);
        AdminHistoriesResponse histories = historyService.getHistories(adminHistoriesCondition);
        return ResponseEntity.ok(histories);
    }

    @GetMapping("/admin/history/{id}")
    public ResponseEntity<AdminHistoryResponse> getHistory(
        @PathVariable(name = "id") Integer id,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        AdminHistoryResponse history = historyService.getHistory(id);
        return ResponseEntity.ok(history);
    }
}
