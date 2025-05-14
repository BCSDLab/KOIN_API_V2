package in.koreatech.koin.domain.shop.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import in.koreatech.koin.domain.shop.dto.order.OrderableShopsFilterCriteria;
import in.koreatech.koin.domain.shop.dto.order.OrderableShopsResponse;
import in.koreatech.koin.domain.shop.dto.order.OrderableShopsSortCriteria;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "(Normal) OrderShop: 주문 가능 상점", description = "주문 가능 상점 정보를 관리한다")
public interface OrderShopApi {

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "주문 가능한 모든 상점 조회 성공",
                content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "성공", value = """
                            [
                              {
                                "shop_id": 2,
                                "name": "가장 맛있는 족발",
                                "is_delivery_available": true,
                                "is_takeout_available": false,
                                "minimum_order_amount": 10000,
                                "rating_average": 4,
                                "review_count": 2,
                                "minimum_delivery_tip": 2500,
                                "maximum_delivery_tip": 4500,
                                "is_open": false,
                                "category_ids": [
                                  1,
                                  3,
                                  5
                                ],
                                "image_urls": [
                                  "https://static.koreatech.in/upload/market/2022/03/26/0e650fe1-811b-411e-9a82-0dd4f43c42ca-1648289492264.jpg"
                                ],
                                "open": [
                                  {
                                    "day_of_week": "MONDAY",
                                    "closed": false,
                                    "open_time": "16:00:00",
                                    "close_time": "00:00:00"
                                  },
                                  {
                                    "day_of_week": "TUESDAY",
                                    "closed": false,
                                    "open_time": "16:00:00",
                                    "close_time": "00:00:00"
                                  },
                                  {
                                    "day_of_week": "WEDNESDAY",
                                    "closed": false,
                                    "open_time": "16:00:00",
                                    "close_time": "00:00:00"
                                  },
                                  {
                                    "day_of_week": "THURSDAY",
                                    "closed": false,
                                    "open_time": "16:00:00",
                                    "close_time": "00:00:00"
                                  },
                                  {
                                    "day_of_week": "FRIDAY",
                                    "closed": false,
                                    "open_time": "16:00:00",
                                    "close_time": "00:00:00"
                                  },
                                  {
                                    "day_of_week": "SATURDAY",
                                    "closed": false,
                                    "open_time": "16:00:00",
                                    "close_time": "00:00:00"
                                  },
                                  {
                                    "day_of_week": "SUNDAY",
                                    "closed": false,
                                    "open_time": "16:00:00",
                                    "close_time": "00:00:00"
                                  }
                                ]
                              }
                            ]
                        """
                    )
                })
            ),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true)))
        }
    )
    @Operation(summary = "주문 가능한 모든 상점 조회", description = """
            ### 주문 가능한 상점 정보 조회
            - **정렬 기준**
                - **null**: 가나다순 (기본)
                - **NONE**: 가나다순 (기본)
                - **COUNT**: 리뷰 개수 내림차순
                - **COUNT_ASC**: 리뷰 개수 오름차순
                - **COUNT_DESC**: 리뷰 개수 내림차순
                - **RATING**: 평점 수 내림차순
                - **RATING_ASC**: 평점 수 오름차순
                - **RATING_DESC**: 평점 수 내림차순
            - **적용 필터**
                - **IS_OPEN**: 현재 영업 중인 상점
                - **DELIVERY_AVAILABLE**: 배달 가능한 상점
                - **TAKEOUT_AVAILABLE**: 포장 가능한 상점
                - **FREE_DELIVERY_TIP**: 배달비 무료 상점
            - **최소 주문 금액 필터**: minimum_order_amount. 해당 최소 주문 금액 이하의 상점을 반환
            """)
    @GetMapping("/order/shops")
    ResponseEntity<List<OrderableShopsResponse>> getOrderableShops(
        @Parameter(description = "정렬 기준. 중복 지정 불가")
        @RequestParam(name = "sorter", defaultValue = "NONE") OrderableShopsSortCriteria sortBy,

        @Parameter(description = "적용할 필터 목록. 복수 선택 가능.")
        @RequestParam(name = "filter", required = false) List<OrderableShopsFilterCriteria> orderableShopsSortCriteria,

        @Parameter(description = "최소 주문 금액 필터 (단위: 원). null 가능")
        @RequestParam(name = "minimum_order_amount", required = false) Integer minimumOrderAmount
    );
}
