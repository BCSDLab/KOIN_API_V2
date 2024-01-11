package in.koreatech.koin.domain.community.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.community.dto.ArticlesResponse;
import in.koreatech.koin.domain.community.service.CommunityService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    @GetMapping("/articles")
    public ResponseEntity<ArticlesResponse> getArticles(@RequestParam Long boardId, @RequestParam(required = false) Long page,
        @RequestParam(required = false) Long limit) {
        ArticlesResponse foundArticles = communityService.getArticles(boardId, page, limit);
        return ResponseEntity.ok().body(foundArticles);
    }
}
