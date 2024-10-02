package in.koreatech.koin.admin.notice.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.admin.notice.dto.AdminNoticeRequest;
import in.koreatech.koin.admin.notice.dto.AdminNoticeResponse;
import in.koreatech.koin.admin.notice.dto.AdminNoticesResponse;
import in.koreatech.koin.admin.user.repository.AdminUserRepository;
import in.koreatech.koin.domain.community.article.model.Article;
import in.koreatech.koin.domain.community.article.model.Board;
import in.koreatech.koin.domain.community.article.model.KoinArticle;
import in.koreatech.koin.domain.community.article.repository.ArticleRepository;
import in.koreatech.koin.domain.community.article.repository.BoardRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.global.model.Criteria;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminNoticeService {

    private final ArticleRepository articleRepository;
    private final AdminUserRepository adminUserRepository;
    private final BoardRepository boardRepository;

    private static final Integer KOIN_ADMIN_NOTICE_BOARD_ID = 9;
    private static final Sort NOTICES_SORT = Sort.by(
        Sort.Order.desc("id")
    );

    @Transactional
    public void createNotice(AdminNoticeRequest request, Integer adminUserId) {
        Board adminNoticeBoard = boardRepository.getById(KOIN_ADMIN_NOTICE_BOARD_ID);
        User adminUser = adminUserRepository.getById(adminUserId);

        KoinArticle koinArticle = KoinArticle.builder()
            .user(adminUser)
            .isDeleted(false)
            .build();

        Article adminNoticeArticle = Article.builder()
            .board(adminNoticeBoard)
            .title(request.title())
            .content(request.content())
            .hit(0)
            .isDeleted(false)
            .attachments(null)
            .isNotice(true)
            .koinArticle(koinArticle)
            .koreatechArticle(null)
            .build();

        koinArticle.setArticle(adminNoticeArticle);

        articleRepository.save(adminNoticeArticle);
    }

    public AdminNoticesResponse getNotices(Integer page, Integer limit, Boolean isDeleted) {
        Integer total = articleRepository.countAllByIsDeletedAndBoardId(isDeleted, KOIN_ADMIN_NOTICE_BOARD_ID);
        Criteria criteria = Criteria.of(page, limit, total);
        PageRequest pageRequest = PageRequest.of(criteria.getPage(), criteria.getLimit(), NOTICES_SORT);
        Page<Article> result = articleRepository.findAllByBoardIdAndIsDeleted(KOIN_ADMIN_NOTICE_BOARD_ID, isDeleted, pageRequest);

        return AdminNoticesResponse.of(result, criteria);
    }

    public AdminNoticeResponse getNotice(Integer noticeId) {
        Article noticeArticle = articleRepository.getAdminNoticeArticleById(noticeId);
        return AdminNoticeResponse.from(noticeArticle);
    }

    @Transactional
    public void deleteNotice(Integer noticeId) {
        Article noticeArticle = articleRepository.getById(noticeId);
        noticeArticle.delete();
    }

    @Transactional
    public void updateNotice(Integer noticeId, AdminNoticeRequest request) {
        Article notice = articleRepository.getById(noticeId);
        notice.updateKoinAdminArticle(request.title(), request.content());
    }
}
