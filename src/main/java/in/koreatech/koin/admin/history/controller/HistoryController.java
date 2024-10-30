package in.koreatech.koin.admin.history.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.admin.history.dto.AdminHistoryResponse;
import in.koreatech.koin.admin.history.dto.AdminHistorysCondition;
import in.koreatech.koin.admin.history.dto.AdminHistorysResponse;
import in.koreatech.koin.admin.history.service.HistoryService;
import in.koreatech.koin.global.auth.Auth;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class HistoryController implements HistoryApi {

    private final HistoryService historyService;

    @GetMapping("/admin/historys")
    public ResponseEntity<AdminHistorysResponse> getHistorys(
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer limit,
        @RequestParam(required = false) String requestMethod,
        @RequestParam(required = false) String domainName,
        @RequestParam(required = false) Integer domainId,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        AdminHistorysCondition adminHistorysCondition = new AdminHistorysCondition(page, limit, requestMethod,
            domainName, domainId);
        AdminHistorysResponse historys = historyService.getHistorys(adminHistorysCondition);
        return ResponseEntity.ok(historys);
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
