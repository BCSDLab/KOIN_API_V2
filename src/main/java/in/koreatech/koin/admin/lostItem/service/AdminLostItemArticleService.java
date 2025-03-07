package in.koreatech.koin.admin.lostItem.service;

import static in.koreatech.koin.domain.shop.model.review.ReportStatus.DELETED;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.admin.lostItem.dto.AdminLostItemArticlesResponse;
import in.koreatech.koin.admin.lostItem.dto.AdminModifyLostItemArticleReportStatusRequest;
import in.koreatech.koin.admin.lostItem.repository.AdminLostItemArticleRepository;
import in.koreatech.koin.domain.community.article.model.LostItemArticle;
import in.koreatech.koin.domain.community.article.model.LostItemReport;
import in.koreatech.koin.domain.shop.model.review.ReportStatus;
import in.koreatech.koin._common.model.Criteria;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminLostItemArticleService {

    private final AdminLostItemArticleRepository adminLostItemArticleRepository;

    public AdminLostItemArticlesResponse getAllLostItemArticles(Integer page, Integer limit) {
        Integer total = adminLostItemArticleRepository.count();
        Criteria criteria = Criteria.of(page, limit, total);
        PageRequest pageRequest = PageRequest.of(criteria.getPage(), criteria.getLimit(),
            Sort.by(Sort.Direction.ASC, "id"));
        Page<LostItemArticle> result = adminLostItemArticleRepository.findAllByIsDeletedFalse(pageRequest);
        return AdminLostItemArticlesResponse.of(result, criteria);
    }

    @Transactional
    public void modifyLostItemArticleReportStatus(Integer id, AdminModifyLostItemArticleReportStatusRequest request) {
        LostItemArticle lostItemArticle = adminLostItemArticleRepository.getByArticleId(id);
        List<LostItemReport> unhandledReports = lostItemArticle.getLostItemReports().stream()
            .filter(report -> report.getReportStatus() == ReportStatus.UNHANDLED)
            .toList();
        unhandledReports.forEach(report -> report.modifyReportStatus(request.reportStatus()));
    }

    @Transactional
    public void deleteLostItemArticle(Integer id) {
        LostItemArticle lostItemArticle = adminLostItemArticleRepository.getByArticleId(id);
        lostItemArticle.getLostItemReports().forEach(report -> report.modifyReportStatus(DELETED));
        lostItemArticle.delete();
    }
}
