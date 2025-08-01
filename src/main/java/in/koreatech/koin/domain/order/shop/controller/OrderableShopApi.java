package in.koreatech.koin.domain.order.shop.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import in.koreatech.koin.domain.order.shop.dto.shopinfo.OrderableShopDeliveryResponse;
import in.koreatech.koin.domain.order.shop.dto.shopinfo.OrderableShopInfoDetailResponse;
import in.koreatech.koin.domain.order.shop.dto.shopinfo.OrderableShopInfoSummaryResponse;
import in.koreatech.koin.domain.order.shop.dto.shoplist.OrderableShopCategoryFilterCriteria;
import in.koreatech.koin.domain.order.shop.dto.shoplist.OrderableShopsFilterCriteria;
import in.koreatech.koin.domain.order.shop.dto.shoplist.OrderableShopsResponse;
import in.koreatech.koin.domain.order.shop.dto.shoplist.OrderableShopsSortCriteria;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "(Normal) OrderShop: 주문 가능 상점", description = "주문 가능 상점 정보를 관리한다")
public interface OrderableShopApi {

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "주문 가능한 모든 상점 조회 성공",
                content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "성공", value = """
                            [
                              {
                                "shop_id": 2,
                                "orderable_shop_id": 1,
                                "name": "가장 맛있는 족발",
                                "is_delivery_available": true,
                                "is_takeout_available": false,
                                "service_event": false,
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
                                "images": [
                                  {
                                    "image_url": "https://static.koreatech.in/upload/market/2022/03/26/0e650fe1-811b-411e-9a82-0dd4f43c42ca-1648289492264.jpg",
                                    "is_thumbnail": true
                                  }
                                ],
                                "open_status": "OPERATING"
                              }
                            ]
                        """
                    )
                })
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 카테고리 ID",
                content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "잘못된 카테고리 ID", value = """
                        {
                          "code": "ILLEGAL_ARGUMENT",
                          "message": "잘못된 인자가 전달되었습니다.",
                          "errorTraceId": "ff8c3490-754e-4ba9-b805-e15132e7f478"
                        }
                        """
                    )
                })
            )
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
        - **카테고리 필터**: category_filter. 카테고리 ID로 상점을 필터링
            - **1 or null**: 전체 카테고리 (기본)
            - **2**: 치킨
            - **3**: 피자/버거
            - **4**: 도시락/분식
            - **5**: 족발
            - **6**: 중국집
            - **7**: 고깃집
            - **8**: 한식
            - **9**: 주점
            - **10**: 카페
            - **11**: 콜밴
            - **12**: 기타
        - **최소 주문 금액 필터**: minimum_order_amount. 해당 최소 주문 금액 이하의 상점을 반환
        - **open_status**: 현재 요일과 시간을 고려 하여 상점의 영업 상태를 결정.
            - **OPERATING**: 영업중
            - **PREPARING**: 영업 준비중
            - **CLOSED**: 영업 종료
        """)
    @GetMapping("/order/shops")
    ResponseEntity<List<OrderableShopsResponse>> getOrderableShops(
        @Parameter(description = "정렬 기준. 중복 지정 불가")
        @RequestParam(name = "sorter", defaultValue = "NONE") OrderableShopsSortCriteria sortBy,

        @Parameter(description = "적용할 필터 목록. 복수 선택 가능.")
        @RequestParam(name = "filter", required = false) List<OrderableShopsFilterCriteria> orderableShopsSortCriteria,

        @Parameter(description = "카테고리 필터 목록")
        @RequestParam(name = "category_filter", required = false) Integer categoryFilterId,

        @Parameter(description = "최소 주문 금액 필터 (단위: 원). null 가능")
        @RequestParam(name = "minimum_order_amount", required = false) Integer minimumOrderAmount
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "주문 가능 상점 요약 정보 조회 성공",
                content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "성공", value = """
                        {
                          "shop_id": 2,
                          "orderable_shop_id": 1,
                          "name": "가장 맛있는 족발",
                          "introduction": "안녕하세요 맛있는 족발입니다. 고객님에게 신선한 음식을 제공하고자 즐거운 하루를 보내시고 감사합니다.",
                          "pay_card": true,
                          "pay_bank": false,
                          "is_delivery_available": true,
                          "is_takeout_available": false,
                          "minimum_order_amount": 10000,
                          "rating_average": 4.5,
                          "review_count": 120,
                          "minimum_delivery_tip": 2000,
                          "maximum_delivery_tip": 5000,
                          "images": [
                            {
                                "image_url": "https://static.koreatech.in/upload/market/2022/03/26/0e650fe1-811b-411e-9a82-0dd4f43c42ca-1648289492264.jpg",
                                "is_thumbnail": true
                            }
                          ]
                        }
                        """
                    )
                })
            ),
            @ApiResponse(responseCode = "404", description = "주문 가능 상점을 찾을 수 없음",
                content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "상점 미존재", value = """
                        {
                          "code": "NOT_FOUND_ORDERABLE_SHOP",
                          "message": "상점이 존재하지 않습니다.",
                          "errorTraceId": "c1c17171-95ad-40ef-87ae-f0223e9614a8"
                        }
                        """
                    )
                })
            )
        }
    )
    @Operation(summary = "특정 주문 가능 상점 요약 정보 조회", description = """
        ### 주문 가능 상점 요약 정보 조회
        - 특정 주문 가능 상점의 요약 정보를 반환합니다.
        - 반환 정보:
            - shop_id: 상점 고유 식별자
            - orderable_shop_id: 주문 가능 상점 고유 식별자
            - name: 상점 이름
            - is_delivery_available: 배달 가능 여부
            - is_takeout_available: 포장 가능 여부
            - pay_card: 카드 결제 가능 여부
            - pay_bank: 계좌 이체 가능 여부
            - minimum_order_amount: 최소 주문 금액
            - rating_average: 평균 평점
            - review_count: 리뷰 수
            - minimum_delivery_tip: 최소 배달비
            - maximum_delivery_tip: 최대 배달비
            - images: 상점 이미지, **존재 하지 않는 경우 빈 배열 반환**
        """)
    @GetMapping("/order/shop/{orderableShopId}/summary")
    ResponseEntity<OrderableShopInfoSummaryResponse> getOrderableShopInfoSummary(
        @Parameter(description = "주문 가능 상점 고유 식별자(orderable_shop_id)", example = "1")
        @PathVariable Integer orderableShopId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "주문 가능 상점 상세 정보 조회 성공",
                content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "성공", value = """
                        {
                          "shop_id": 2,
                          "orderable_shop_id": 1,
                          "name": "가장 맛있는 족발",
                          "address": "천안시 동남구 충절로 880 가동 1층",
                          "open_time": "16:00:00",
                          "close_time": "00:00:00",
                          "closed_days": [],
                          "phone": "041-523-5849",
                          "introduction": "안녕하세요 맛있는 족발입니다. 고객님에게 신선한 음식을 제공하고자 즐거운 하루를 보내시고 감사합니다.",
                          "notice": "*행사 이벤트 진행중입니다* 단체 주문 시 20% 할인 합니다. 50,000원 이상 주문 시 막국수 서비스 드립니다",
                          "delivery_tips": [
                            {
                              "from_amount": 10000,
                              "to_amount": 18000,
                              "fee": 4500
                            },
                            {
                              "from_amount": 18000,
                              "to_amount": null,
                              "fee": 2500
                            }
                          ],
                          "owner_info": {
                            "name": "홍길동",
                            "shop_name": "가장 맛있는 족발",
                            "address": "천안시 동남구 충절로 880 가동 1층",
                            "company_registration_number": "123-21-31313"
                          },
                          "origins": [
                            {
                              "ingredient": "소고기",
                              "origin": "국내산 한우"
                            },
                            {
                              "ingredient": "쌀",
                              "origin": "국내산"
                             }
                          ]
                        }
                        """
                    )
                })
            ),
            @ApiResponse(responseCode = "404", description = "주문 가능 상점을 찾을 수 없음",
                content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "상점 미존재", value = """
                        {
                          "code": "NOT_FOUND_ORDERABLE_SHOP",
                          "message": "상점이 존재하지 않습니다.",
                          "errorTraceId": "c1c17171-95ad-40ef-87ae-f0223e9614a8"
                        }
                        """
                    )
                })
            )
        }
    )
    @Operation(summary = "특정 주문 가능 상점 가게 정보·원산지 조회", description = """
        ### 주문 가능 상점 가게 정보·원산지 조회
        - 특정 주문 가능 상점의 가게 정보·원산지 조회 결과를 반환합니다.
        - 반환 정보:
            - shop_id: 상점 고유 식별자
            - orderable_shop_id: 주문 가능 상점 고유 식별자
            - name: 상점 이름
            - address: 상점 주소
            - open_time: 영업 시작 시간 (오늘 요일 기준, 영업 하지 않는 날은 null), **nullable**
            - close_time: 영업 종료 시간 (오늘 요일 기준, 영업 하지 않는 날은 null), **nullable**
            - closed_days: 휴무 요일 목록, **empty array**
            - phone: 상점 전화번호 (해당 데이터 없으면 empty array 반환)
            - introduction: 가게 소개, **nullable**
            - notice: 가게 알림, **nullable**
            - delivery_tips: 주문 금액별 총 배달팁
            - owner_info: 사업자 정보
            - owner_info.name: **nullable**
            - owner_info.shop_name: **nullable**
            - owner_info.address: **nullable**
            - owner_info.company_registration_number: **nullable**
            - origins: 원산지 표기  (해당 데이터 없으면 empty array 반환)
        """)
    @GetMapping("/order/shop/{orderableShopId}/detail")
    ResponseEntity<OrderableShopInfoDetailResponse> getOrderableShopInfoDetail(
        @Parameter(description = "주문 가능 상점 고유 식별자(orderable_shop_id)", example = "1")
        @PathVariable Integer orderableShopId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "주문 가능 상점 상세 정보 조회 성공",
                content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "성공", value = """
                        {
                           "campus_delivery": true,
                           "off_campus_delivery": true
                        }
                        """
                    )
                })
            ),
            @ApiResponse(responseCode = "404", description = "주문 가능 상점을 찾을 수 없음",
                content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "상점 미존재", value = """
                        {
                          "code": "NOT_FOUND_ORDERABLE_SHOP",
                          "message": "상점이 존재하지 않습니다.",
                          "errorTraceId": "c1c17171-95ad-40ef-87ae-f0223e9614a8"
                        }
                        """
                    )
                })
            )
        }
    )
    @Operation(
        summary = "특정 주문 가능 상점 교외/교내 배달 여부 조회",
        description = """
            ## 특정 주문 가능 상점 교외/교내 배달 여부 조회
            - 주문 상점의 교외 배달 / 교내 배달 가능 여부를 반환 합니다.
            - nullable
                - **campus_delivery** : X
                - **off_campus_delivery** : X
            """
    )
    @GetMapping("/order/shop/{orderableShopId}/delivery")
    ResponseEntity<OrderableShopDeliveryResponse> getOrderableShopDeliveryResponse(
        @Parameter(description = "주문 가능 상점 고유 식별자(orderable_shop_id)", example = "1")
        @PathVariable Integer orderableShopId
    );
}
