package in.koreatech.koin.domain.shop.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import in.koreatech.koin.domain.shop.dto.search.response.ShopSearchRelatedKeywordResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "(Normal) ShopSearch: 상점 검색", description = "상점 정보를 검색한다")
public interface ShopSearchApi {

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "주변 상점 검색 연관 키워드 조회 성공",
                content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "연관 키워드 존재", value = """
                            {
                              "search_keyword": "치킨ㅋ",
                              "processed_search_keyword": "치킨",
                              "shop_name_search_result_count": 1,
                              "menu_name_search_result_count": 2,
                              "shop_name_search_results": [
                                {
                                  "shop_id": 2,
                                  "shop_name": "굿모닝살로만치킨"
                                }
                              ],
                              "menu_name_search_results": [
                                {
                                  "shop_id": 2,
                                  "shop_name": "굿모닝살로만치킨",
                                  "menu_name": "후라이드 치킨"
                                },
                                {
                                  "shop_id": 5,
                                  "shop_name": "마슬랜",
                                  "menu_name": "후라이드 치킨"
                                }
                              ]
                            }
                        """
                    ),
                    @ExampleObject(name = "연관 키워드 없음", value = """
                            {
                              "search_keyword": "마라탕ㅏ",
                              "processed_search_keyword": "마라탕",
                              "shop_name_search_result_count": 0,
                              "menu_name_search_result_count": 0,
                              "shop_name_search_results": [],
                              "menu_name_search_results": []
                            }
                        """
                    )
                })
            )
        }
    )
    @Operation(summary = "주변 상점 검색 연관 키워드 조회", description = """
        ### 주변 상점 검색 연관 키워드 조회
        - 주변 상점 검색 시 연관 검색어 표시 화면에서 사용 되는 API 입니다.
        - 상점 이름 중, 주어진 키워드와 일치하는 주변 상점 이름, 식별자 목록을 반환합니다.
        - 메뉴 이름 중, 주어진 키워드와 일치하는 메뉴 이름과 해당 메뉴를 판매하는 주변 상점 이름, 식별자 목록을 반환합니다.
        - **검색 키워드 처리**
            - 키워드가 미완성된 자음/모음을 포함하는 경우 제거됩니다. ex) "치킨ㅋ" => "치킨"
            - 검색 키워드가 "페리카나 치킨" 인 경우 공백을 기준으로 "페리카나", "치킨" 두개의 키워드로 분리되어 OR 조건으로 검색합니다.
        """)
    @GetMapping("/v2/shops/search/related")
    ResponseEntity<ShopSearchRelatedKeywordResponse> getRelatedKeyword(
        @RequestParam(name = "keyword") String keyword
    );
}
