package in.koreatech.koin.domain.order.shop.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import in.koreatech.koin.domain.order.shop.dto.shopsearch.OrderableShopSearchRelatedKeywordResponse;
import in.koreatech.koin.domain.order.shop.dto.shopsearch.OrderableShopSearchResultResponse;
import in.koreatech.koin.domain.order.shop.dto.shopsearch.OrderableShopSearchResultSortCriteria;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "(Normal) OrderableShopSearch: 주문 가능 상점 검색", description = "주문 가능 상점을 검색한다")
public interface OrderableShopSearchApi {

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "주문 가능 상점 검색 연관 키워드 조회 성공",
                content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "연관 키워드 존재", value = """
                            {
                              "search_keyword": "치킨ㅋ",
                              "processed_search_keyword": "치킨",
                              "shop_name_search_result_count": 1,
                              "menu_name_search_result_count": 2,
                              "shop_name_search_results": [
                                {
                                  "orderable_shop_id": 2,
                                  "orderable_shop_name": "굿모닝살로만치킨"
                                }
                              ],
                              "menu_name_search_results": [
                                {
                                  "orderable_shop_id": 2,
                                  "orderable_shop_name": "굿모닝살로만치킨",
                                  "menu_name": "후라이드 치킨"
                                },
                                {
                                  "orderable_shop_id": 5,
                                  "orderable_shop_name": "마슬랜",
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
    @Operation(summary = "주문 가능 상점 검색 연관 키워드 조회", description = """
        ### 주문 가능 상점 검색 연관 키워드 조회
        - 주문 가능 상점 검색 시 연관 검색어 표시 화면에서 사용 되는 API 입니다.
        - 상점 이름 중, 주어진 키워드와 일치하는 주문 가능 상점 이름, 식별자 목록을 반환합니다.
        - 메뉴 이름 중, 주어진 키워드와 일치하는 메뉴 이름과 해당 메뉴를 판매하는 주문 가능 상점 이름, 식별자 목록을 반환합니다.
        - **검색 키워드 처리**
            - 키워드에서 공백이 제거됩니다.
            - 키워드가 미완성된 자음/모음을 포함하는 경우 제거됩니다. ex) "치킨ㅋ" => "치킨"
        """)
    @GetMapping("/order/shop/search/{keyword}/related")
    ResponseEntity<OrderableShopSearchRelatedKeywordResponse> searchRelatedKeyword(
        @PathVariable(name = "keyword") String keyword
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "주문 가능 상점 검색 성공",
                content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "검색 결과 존재", value = """
                            {
                              "search_keyword": "치킨ㅋ",
                              "processed_search_keyword": "치킨",
                              "result_count": 1,
                              "search_results": [
                                {
                                  "orderable_shop_id": 2,
                                  "name": "굿모닝살로만치킨",
                                  "is_delivery_available": true,
                                  "is_takeout_available": true,
                                  "service_event": false,
                                  "minimum_order_amount": 15000,
                                  "rating_average": 4,
                                  "review_count": 189,
                                  "minimum_delivery_tip": 1500,
                                  "maximum_delivery_tip": 3500,
                                  "is_open": true,
                                  "thumbnail_image_url": "https://static.koreatech.in/upload/test.jpg",
                                  "open_status": "OPERATING",
                                  "contain_menu_name": [
                                    "후라이드 치킨",
                                    "양념 치킨"
                                  ]
                                }
                              ]
                            }
                        """
                    ),
                    @ExampleObject(name = "검색 결과 없음", value = """
                            {
                              "search_keyword": "마라탕ㅏ",
                              "processed_search_keyword": "마라탕",
                              "result_count": 0,
                              "search_results": []
                            }
                        """
                    )
                })
            )
        }
    )
    @Operation(summary = "주문 가능 상점 검색", description = """
        ### 주문 가능 상점 검색
        - 주문 가능 상점 검색 결과 화면에서 사용 되는 API 입니다.
        - 키워드를 이용하여 메뉴 이름과 상점 이름을 기준으로 상점을 검색합니다.
        - **정렬 기준**
            - **null**: 가나다순 (기본)
            - **NONE**: 가나다순 (기본)
            - **REVIEW_COUNT**: 리뷰 개수 내림차순
            - **REVIEW_COUNT_ASC**: 리뷰 개수 오름차순
            - **REVIEW_COUNT_DESC**: 리뷰 개수 내림차순
            - **RATING**: 평점 수 내림차순
            - **RATING_ASC**: 평점 수 오름차순
            - **RATING_DESC**: 평점 수 내림차순
        - **open_status**: 현재 요일과 시간을 고려 하여 상점의 영업 상태를 결정.
            - **OPERATING**: 영업중
            - **PREPARING**: 영업 준비중
            - **CLOSED**: 영업 종료
        - 검색 결과가 존재 하지 않는 경우 **search_results** 필드는 빈 배열을 반환 합니다.
        - **검색 키워드 처리**
            - 키워드에서 공백이 제거됩니다.
            - 키워드가 미완성된 자음/모음을 포함하는 경우 제거됩니다. ex) "치킨ㅋ" => "치킨"
        """)
    @GetMapping("/order/shop/search/{keyword}")
    ResponseEntity<OrderableShopSearchResultResponse> searchOrderableShop(
        @RequestParam(name = "sorter", defaultValue = "NONE") OrderableShopSearchResultSortCriteria sortBy,
        @PathVariable(name = "keyword") String keyword
    );
}
