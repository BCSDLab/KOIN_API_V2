package in.koreatech.koin.domain.order.shop.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import in.koreatech.koin.domain.order.shop.dto.menu.OrderableShopMenuGroupResponse;
import in.koreatech.koin.domain.order.shop.dto.menu.OrderableShopMenuResponse;
import in.koreatech.koin.domain.order.shop.dto.menu.OrderableShopMenusResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "(Normal) OrderShopMenu: 주문 가능 상점 메뉴", description = "주문 가능 상점 메뉴를 관리한다")
public interface OrderableShopMenuApi {

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "주문 가능 상점의 모든 메뉴 조회 성공",
                content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "성공", value = """
                        [
                          {
                            "menu_group_id": 1,
                            "menu_group_name": "메인 메뉴",
                            "menus": [
                              {
                                "id": 101,
                                "name": "후라이드 치킨",
                                "description": "바삭하고 고소한 오리지널 후라이드",
                                "thumbnail_image": "https://example.com/images/fried_chicken.jpg",
                                "prices": [
                                  {
                                    "id": 1,
                                    "name": null,
                                    "price": 19000
                                  }
                                ]
                              },
                              {
                                "id": 102,
                                "name": "양념 치킨",
                                "description": "달콤매콤한 특제 양념 소스",
                                "thumbnail_image": "https://example.com/images/seasoned_chicken.jpg",
                                "prices": [
                                  {
                                    "id": 2,
                                    "name": null,
                                    "price": 20000
                                  }
                                ]
                              }
                            ]
                          },
                          {
                            "menu_group_id": 2,
                            "menu_group_name": "사이드 메뉴",
                            "menus": [
                              {
                                "id": 201,
                                "name": "감자튀김",
                                "description": "치킨과 함께 먹으면 더욱 맛있는 바삭한 감자튀김",
                                "thumbnail_image": "https://example.com/images/fries.jpg",
                                "prices": [
                                  {
                                    "id": 3,
                                    "name": "중",
                                    "price": 4000
                                  },
                                  {
                                    "id": 4,
                                    "name": "대",
                                    "price": 6000
                                  }
                                ]
                              }
                            ]
                          }
                        ]
                        """
                    )
                })
            ),
            @ApiResponse(responseCode = "404", description = "주문 가능 상점을 찾을 수 없음",
                content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "상점 미존재", value = """
                        {
                          "status": 404,
                          "error": "Not Found",
                          "message": "해당 상점이 존재하지 않습니다 : 1"
                        }
                        """
                    )
                })
            )
        }
    )
    @Operation(summary = "특정 주문 가능 상점의 모든 메뉴 조회", description = """
        ### 주문 가능 상점의 모든 메뉴 정보 조회
        - 특정 주문 가능 상점의 모든 메뉴를 메뉴 그룹별로 묶어서 반환합니다.
        - `prices` 필드가 배열인 이유는 '소', '중', '대' 와 같이 하나의 메뉴에 여러 가격이 존재할 수 있기 때문입니다.
        - `prices.name`이 `null`인 경우 단일 가격 메뉴를 의미합니다.
        """)
    @GetMapping("/order/shop/{orderableShopId}/menus")
    ResponseEntity<List<OrderableShopMenusResponse>> getOrderableShopMenus(
        @Parameter(description = "주문 가능 상점 고유 식별자 (orderable_shop_id)", example = "1")
        @PathVariable Integer orderableShopId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "특정 메뉴의 상세 정보 조회 성공",
                content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "성공", value = """
                        {
                          "id": 101,
                          "name": "후라이드 치킨",
                          "description": "바삭하고 고소한 오리지널 후라이드",
                          "images": [
                            "https://example.com/images/fried_chicken_1.jpg",
                            "https://example.com/images/fried_chicken_2.jpg"
                          ],
                          "prices": [
                                  {
                                    "id": 1,
                                    "name": null,
                                    "price": 19000
                                  }
                          ],
                          "option_groups": [
                            {
                              "id": 1,
                              "name": "음료 변경",
                              "description": "기본 제공 음료를 변경할 수 있습니다.",
                              "is_required": false,
                              "min_select": 0,
                              "max_select": 1,
                              "options": [
                                {
                                  "id": 1,
                                  "name": "콜라 1.25L로 변경",
                                  "price": 1000
                                },
                                {
                                  "id": 2,
                                  "name": "사이다 1.25L로 변경",
                                  "price": 1000
                                }
                              ]
                            },
                            {
                              "id": 2,
                              "name": "소스 추가",
                              "description": "다양한 소스를 추가해보세요!",
                              "is_required": false,
                              "min_select": 0,
                              "max_select": 3,
                              "options": [
                                {
                                  "id": 3,
                                  "name": "양념 소스",
                                  "price": 500
                                },
                                {
                                  "id": 4,
                                  "name": "머스타드 소스",
                                  "price": 500
                                },
                                {
                                  "id": 5,
                                  "name": "치즈 소스",
                                  "price": 1000
                                }
                              ]
                            }
                          ]
                        }
                        """
                    )
                })
            ),
            @ApiResponse(responseCode = "404", description = "메뉴 또는 상점을 찾을 수 없음",
                content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "메뉴 미존재", value = """
                        {
                          "status": 404,
                          "error": "Not Found",
                          "message": "해당 메뉴가 존재하지 않습니다 : 999"
                        }
                        """
                    )
                })
            )
        }
    )
    @Operation(summary = "특정 메뉴의 상세 정보 조회", description = """
        ### 주문 가능 상점의 특정 메뉴 상세 정보 조회
        - 메뉴의 상세 정보(이미지, 옵션 등)를 조회합니다.
        - `orderableShopId`는 현재 라우팅 경로상 필요하지만, 실제 로직에서는 `orderableShopMenuId`만 사용하여 메뉴를 조회합니다.
        """)
    @GetMapping("/order/shop/{orderableShopId}/menus/{orderableShopMenuId}")
    ResponseEntity<OrderableShopMenuResponse> getOrderableShopMenu(
        @Parameter(description = "주문 가능 상점 고유 식별자 (orderable_shop_id)", example = "1")
        @PathVariable Integer orderableShopId,

        @Parameter(description = "주문 가능 상점 메뉴 고유 식별자 (orderable_shop_menu_id)", example = "1")
        @PathVariable Integer orderableShopMenuId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "주문 가능 상점 메뉴 그룹 목록 조회 성공",
                content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "성공", value = """
                        {
                          "count": 2,
                          "menuGroups": [
                            {
                              "id": 1,
                              "name": "메인 메뉴"
                            },
                            {
                              "id": 2,
                              "name": "사이드 메뉴"
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
                          "status": 404,
                          "error": "Not Found",
                          "message": "해당 상점이 존재하지 않습니다 : 1"
                        }
                        """
                    )
                })
            )
        }
    )
    @Operation(summary = "특정 주문 가능 상점의 메뉴 그룹 목록 조회", description = """
        ### 주문 가능 상점 메뉴 그룹 목록 조회
        - 특정 주문 가능 상점 모든 메뉴 그룹 ID와 이름을 반환합니다.
        """)
    @GetMapping("/order/shop/{orderableShopId}/menus/groups")
    ResponseEntity<OrderableShopMenuGroupResponse> getOrderableShopMenuGroups(
        @Parameter(description = "주문 가능 상점 고유 식별자 (orderable_shop_id)", example = "1")
        @PathVariable Integer orderableShopId
    );
}
