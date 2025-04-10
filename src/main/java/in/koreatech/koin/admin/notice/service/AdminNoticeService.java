package in.koreatech.koin.admin.notice.service;

import static in.koreatech.koin.domain.community.article.model.Board.KOIN_ADMIN_NOTICE_BOARD_ID;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin._common.model.Criteria;
import in.koreatech.koin.admin.notice.dto.AdminNoticeRequest;
import in.koreatech.koin.admin.notice.dto.AdminNoticeResponse;
import in.koreatech.koin.admin.notice.dto.AdminNoticesResponse;
import in.koreatech.koin.admin.notice.repository.AdminKoinArticleRepository;
import in.koreatech.koin.admin.notice.repository.AdminKoreatechArticleRepository;
import in.koreatech.koin.admin.notice.repository.AdminNoticeRepository;
import in.koreatech.koin.admin.user.model.Admin;
import in.koreatech.koin.admin.user.repository.AdminRepository;
import in.koreatech.koin.domain.community.article.model.Article;
import in.koreatech.koin.domain.community.article.model.Board;
import in.koreatech.koin.domain.community.article.repository.BoardRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminNoticeService {

    private final AdminNoticeRepository adminNoticeRepository;
    private final AdminRepository adminRepository;
    private final BoardRepository boardRepository;

    private static final Sort NOTICES_SORT = Sort.by(
        Sort.Order.desc("id")
    );
    private final AdminKoinArticleRepository adminKoinArticleRepository;
    private final AdminKoreatechArticleRepository adminKoreatechArticleRepository;

    @Transactional
    public void createNotice(AdminNoticeRequest request, Integer adminUserId) {
        Board adminNoticeBoard = boardRepository.getById(KOIN_ADMIN_NOTICE_BOARD_ID);
        Admin adminUser = adminRepository.getById(adminUserId);
        Article adminNoticeArticle = Article.createKoinNotice(request, adminNoticeBoard, adminUser);
        adminNoticeRepository.save(adminNoticeArticle);
    }

    public AdminNoticesResponse getNotices(Integer page, Integer limit, Boolean isDeleted) {
        Integer total = adminNoticeRepository.countAllByIsDeletedAndBoardId(isDeleted, KOIN_ADMIN_NOTICE_BOARD_ID);
        Criteria criteria = Criteria.of(page, limit, total);
        PageRequest pageRequest = PageRequest.of(criteria.getPage(), criteria.getLimit(), NOTICES_SORT);
        Page<Article> result = adminNoticeRepository.findAllByBoardIdAndIsDeleted(KOIN_ADMIN_NOTICE_BOARD_ID, isDeleted,
            pageRequest);
        result.forEach(this::setAuthorName);
        return AdminNoticesResponse.of(result, criteria);
    }

    private void setAuthorName(Article article) {
        adminKoinArticleRepository.findByArticleId(article.getId())
            .ifPresent(koinArticle -> article.setAuthor(koinArticle.getUser().getName()));
        adminKoreatechArticleRepository.findByArticleId(article.getId())
            .ifPresent(koreatechArticle -> article.setAuthor(koreatechArticle.getAuthor()));
    }

    public AdminNoticeResponse getNotice(Integer noticeId) {
        Article article = adminNoticeRepository.getNoticeById(noticeId);
        setAuthorName(article);
        return AdminNoticeResponse.from(article);
    }

    @Transactional
    public void deleteNotice(Integer noticeId) {
        Optional<Article> foundArticle = adminNoticeRepository.findByIdAndIsDeleted(noticeId, false);
        if (foundArticle.isEmpty()) {
            return;
        }
        foundArticle.get().delete();
    }

    @Transactional
    public void updateNotice(Integer noticeId, AdminNoticeRequest request) {
        Article notice = adminNoticeRepository.getNoticeById(noticeId);
        notice.updateKoinAdminArticle(request.title(), request.content());
    }
}
