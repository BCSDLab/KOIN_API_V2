package in.koreatech.koin.domain.community.article.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.community.article.dto.ArticleResponseV2;
import in.koreatech.koin.domain.community.article.service.ArticleService;
import in.koreatech.koin.global.ipaddress.IpAddress;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v2/articles")
public class ArticleControllerV2 implements ArticleApiV2 {

    private final ArticleService articleService;

    @GetMapping("/{id}")
    public ResponseEntity<ArticleResponseV2> getArticleV2(
        @RequestParam(required = false) Integer boardId,
        @PathVariable("id") Integer articleId,
        @IpAddress String ipAddress
    ) {
        ArticleResponseV2 foundArticle = articleService.getArticleV2(boardId, articleId, ipAddress);
        return ResponseEntity.ok().body(foundArticle);
    }
}
