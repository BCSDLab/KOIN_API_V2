package in.koreatech.koin.domain.order.cart.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import in.koreatech.koin.domain.order.cart.dto.CartAddItemRequest;
import in.koreatech.koin.domain.order.cart.dto.CartMenuItemEditResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "(Normal) Cart: 장바구니", description = "장바구니를 관리한다")
public interface CartApi {

    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "장바구니 상품 추가 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청",
            content = @Content(mediaType = "application/json", examples = {
                @ExampleObject(name = "다른 상점의 상품을 담은 경우", summary = "다른 상점 상품 추가", value = """
                    {
                      "code": "DIFFERENT_SHOP_ITEM_IN_CART",
                      "message": "장바구니에는 동일한 상점의 상품만 담을 수 있습니다.",
                      "errorTraceId": "a1b2c3d4-e5f6-7890-1234-567890abcdef"
                    }
                    """),
                @ExampleObject(name = "메뉴가 품절된 경우", summary = "품절된 메뉴", value = """
                    {
                      "code": "MENU_SOLD_OUT",
                      "message": "상품이 매진되었습니다",
                      "errorTraceId": "a1b2c3d4-e5f6-7890-1234-567890abcdef"
                    }
                    """),
                @ExampleObject(name = "필수 옵션을 선택하지 않은 경우", summary = "필수 옵션 누락", value = """
                    {
                      "code": "REQUIRED_OPTION_GROUP_MISSING",
                      "message": "필수 옵션 그룹을 선택하지 않았습니다.",
                      "errorTraceId": "a1b2c3d4-e5f6-7890-1234-567890abcdef"
                    }
                    """),
                @ExampleObject(name = "옵션 최대 선택 개수를 초과한 경우", summary = "옵션 선택 초과", value = """
                    {
                      "code": "MAX_SELECTION_EXCEEDED",
                      "message": "옵션 그룹의 최대 선택 개수를 초과했습니다.",
                      "errorTraceId": "a1b2c3d4-e5f6-7890-1234-567890abcdef"
                    }
                    """),
                @ExampleObject(name = "선택한 메뉴가 해당 상점에 속해 있지 않은 경우", summary = "선택 메뉴와 상점 메뉴 불일치", value = """
                    {
                      "code": "INVALID_MENU_IN_SHOP",
                      "message": "선택한 메뉴는 해당 상점에 속해있지 않습니다",
                      "errorTraceId": "9e8e6bfb-d417-4a1e-a601-a7bbc2ff5c4f"
                    }
                    """),
                @ExampleObject(name = "잘못된 입력 형식", summary = "잘못된 입력 형식", value = """
                    {
                      "code": "",
                      "message": "잘못된 입력 형식이거나, 값이 허용된 범위를 초과했습니다.",
                      "errorTraceId": "6a5ef873-c752-4387-9dae-552798812f25"
                    }
                    """)
            })
        ),
        @ApiResponse(responseCode = "401", description = "인증 정보 오류",
            content = @Content(mediaType = "application/json", examples = {
                @ExampleObject(name = "인증 정보 오류", summary = "인증 정보 오류", value = """
                    {
                      "code": "",
                      "message": "올바르지 않은 인증정보입니다.",
                      "errorTraceId": "5ba40351-6d27-40e5-90e3-80c5cf08a1ac"
                    }
                    """)
            })
        ),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 리소스",
            content = @Content(mediaType = "application/json", examples = {
                @ExampleObject(name = "유효하지 않은 메뉴 가격 ID", summary = "메뉴 가격 ID 없음", value = """
                    {
                      "code": "MENU_PRICE_NOT_FOUND",
                      "message": "유효하지 않은 가격 ID 입니다.",
                      "errorTraceId": "a1b2c3d4-e5f6-7890-1234-567890abcdef"
                    }
                    """),
                @ExampleObject(name = "유효하지 않은 메뉴 옵션 ID", summary = "메뉴 옵션 ID 없음", value = """
                    {
                      "code": "MENU_OPTION_NOT_FOUND",
                      "message": "유효하지 않은 옵션 ID 입니다.",
                      "errorTraceId": "a1b2c3d4-e5f6-7890-1234-567890abcdef"
                    }
                    """)
            })
        )
    })
    @Operation(summary = "장바구니에 상품 담기", description = """
        ### 장바구니에 메뉴 추가
        - 인증된 사용자의 장바구니에 지정된 메뉴와 옵션을 추가합니다.
        - **주의사항**: 장바구니에는 **동일한 상점의 상품**만 담을 수 있습니다. 다른 상점의 상품을 담으려고 시도하면 `400 Bad Request (DIFFERENT_SHOP_ITEM_IN_CART)` 오류가 발생합니다.

        ### 요청 Body 필드 설명
        - **orderableShopId**: 주문 가능 상점 ID (필수)
        - **orderableShopMenuId**: 주문 가능 상점 메뉴 ID (필수)
        - **orderableShopMenuPriceId**: 주문 가능 상점 메뉴 가격 ID (필수)
        - **orderableShopMenuOptionIds**: 선택한 옵션 목록 (선택 사항)
          - **optionGroupId**: 옵션 그룹 ID
          - **optionId**: 옵션 ID
        """)
    @PostMapping("/cart/add")
    ResponseEntity<Void> addItem(
        @RequestBody @Valid CartAddItemRequest cartAddItemRequest,
        @Parameter(hidden = true) Integer userId
    );

    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "장바구니 상품 수량 변경 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청",
            content = @Content(mediaType = "application/json", examples = {
                @ExampleObject(name = "수량이 1 미만인 경우", summary = "수량 1 미만", value = """
                    {
                      "code": "INVALID_QUANTITY",
                      "message": "유효하지 않은 수량입니다. 수량은 1 이상이어야 합니다.",
                      "errorTraceId": "a1b2c3d4-e5f6-7890-1234-567890abcdef"
                    }
                    """),
                @ExampleObject(name = "수량이 null인 경우", summary = "수량 null", value = """
                    {
                      "code": "INVALID_QUANTITY",
                      "message": "유효하지 않은 수량입니다. 수량은 null일 수 없습니다.",
                      "errorTraceId": "a1b2c3d4-e5f6-7890-1234-567890abcdef"
                    }
                    """)
            })
        ),
        @ApiResponse(responseCode = "401", description = "인증 정보 오류",
            content = @Content(mediaType = "application/json", examples = {
                @ExampleObject(name = "인증 정보 오류", summary = "인증 정보 오류", value = """
                    {
                      "code": "",
                      "message": "올바르지 않은 인증정보입니다.",
                      "errorTraceId": "5ba40351-6d27-40e5-90e3-80c5cf08a1ac"
                    }
                    """)
            })
        ),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 리소스",
            content = @Content(mediaType = "application/json", examples = {
                @ExampleObject(name = "장바구니 상품을 찾을 수 없는 경우", summary = "존재하지 않는 장바구니 상품", value = """
                    {
                      "code": "CART_MENU_ITEM_NOT_FOUND",
                      "message": "장바구니에 담긴 상품이 존재하지 않습니다",
                      "errorTraceId": "a1b2c3d4-e5f6-7890-1234-567890abcdef"
                    }
                    """),
                @ExampleObject(name = "장바구니가 없는 경우", summary = "장바구니 없음", value = """
                    {
                      "code": "CART_NOT_FOUND",
                      "message": "장바구니를 찾을 수 없습니다.",
                      "errorTraceId": "a1b2c3d4-e5f6-7890-1234-567890abcdef"
                    }
                    """)
            })
        )
    })
    @Operation(summary = "장바구니 상품 수량 변경", description = """
        ### 장바구니 상품 수량 변경
        - 장바구니에 담긴 특정 상품(`cartMenuItemId`)의 수량을 변경합니다.
        - 수량은 **1 이상**의 정수여야 합니다.
        
        ### 권한 및 제약사항
        - 자신의 장바구니에 담긴 상품의 수량만 변경할 수 있습니다.
        - 존재하지 않는 장바구니 상품 ID로 요청하거나, 자신의 장바구니에 없는 상품 ID로 요청 시 `404 Not Found (CART_MENU_ITEM_NOT_FOUND)` 오류가 발생합니다.
        """)
    @PostMapping("/cart/quantity/{cartMenuItemId}/{quantity}")
    ResponseEntity<Void> updateItemQuantity(
        @Parameter(description = "수량을 변경할 장바구니 상품의 고유 ID", example = "1", required = true)
        @PathVariable Integer cartMenuItemId,

        @Parameter(description = "변경할 수량 (1 이상)", example = "3", required = true)
        @PathVariable Integer quantity,

        @Parameter(hidden = true)
        Integer userId
    );

    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "장바구니 상품 삭제 성공"),
        @ApiResponse(responseCode = "401", description = "인증 정보 오류",
            content = @Content(mediaType = "application/json", examples = {
                @ExampleObject(name = "인증 정보 오류", summary = "인증 정보 오류", value = """
                    {
                      "code": "",
                      "message": "올바르지 않은 인증정보입니다.",
                      "errorTraceId": "5ba40351-6d27-40e5-90e3-80c5cf08a1ac"
                    }
                    """)
            })
        ),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 리소스",
            content = @Content(mediaType = "application/json", examples = {
                @ExampleObject(name = "장바구니 상품을 찾을 수 없는 경우", summary = "존재하지 않는 장바구니 상품", value = """
                    {
                      "code": "CART_MENU_ITEM_NOT_FOUND",
                      "message": "장바구니에 담긴 상품이 존재하지 않습니다",
                      "errorTraceId": "a1b2c3d4-e5f6-7890-1234-567890abcdef"
                    }
                    """),
                @ExampleObject(name = "장바구니가 없는 경우", summary = "장바구니 없음", value = """
                    {
                      "code": "CART_NOT_FOUND",
                      "message": "장바구니를 찾을 수 없습니다.",
                      "errorTraceId": "a1b2c3d4-e5f6-7890-1234-567890abcdef"
                    }
                    """)
            })
        )
    })
    @Operation(summary = "장바구니 상품 삭제", description = """
        ### 장바구니 상품 삭제
        - 장바구니에 담긴 특정 상품(`cartMenuItemId`)를 삭제합니다.
        
        ### 권한 및 제약사항
        - 자신의 장바구니에 담긴 상품만 삭제할 수 있습니다.
        - 존재하지 않는 장바구니 상품 ID로 요청하거나, 자신의 장바구니에 없는 장바구니 상품 ID로 요청 시 `404 Not Found (CART_MENU_ITEM_NOT_FOUND)` 오류가 발생합니다.
        """)
    @DeleteMapping("/cart/delete/{cartMenuItemId}")
    ResponseEntity<Void> deleteItem(
        @Parameter(description = "제거할 장바구니 상품의 고유 ID", example = "1", required = true)
        @PathVariable Integer cartMenuItemId,

        @Parameter(hidden = true)
        Integer userId
    );

    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "장바구니 초기화 성공"),
        @ApiResponse(responseCode = "401", description = "인증 정보 오류",
            content = @Content(mediaType = "application/json", examples = {
                @ExampleObject(name = "인증 정보 오류", summary = "인증 정보 오류", value = """
                    {
                      "code": "",
                      "message": "올바르지 않은 인증정보입니다.",
                      "errorTraceId": "5ba40351-6d27-40e5-90e3-80c5cf08a1ac"
                    }
                    """)
            })
        ),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 리소스",
            content = @Content(mediaType = "application/json", examples = {
                @ExampleObject(name = "장바구니가 없는 경우", summary = "장바구니 없음", value = """
                    {
                      "code": "CART_NOT_FOUND",
                      "message": "장바구니를 찾을 수 없습니다.",
                      "errorTraceId": "a1b2c3d4-e5f6-7890-1234-567890abcdef"
                    }
                    """)
            })
        )
    })
    @Operation(summary = "장바구니 초기화", description = """
        ### 장바구니 초기화
        - 장바구니에 담긴 모든 상품을 삭제합니다.
        """)
    @DeleteMapping("/cart/reset")
    ResponseEntity<Void> reset(
        @Parameter(hidden = true)
        Integer userId
    );

    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "선택 필드가 포함된 특정 메뉴의 상세 정보 조회 성공",
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
                                    "price": 19000,
                                    "is_selected": true
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
                                  "price": 1000,
                                  "is_selected": false
                                },
                                {
                                  "id": 2,
                                  "name": "사이다 1.25L로 변경",
                                  "price": 1000,
                                  "is_selected": true
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
                                  "price": 500,
                                  "is_selected": true
                                },
                                {
                                  "id": 4,
                                  "name": "머스타드 소스",
                                  "price": 500,
                                  "is_selected": false
                                },
                                {
                                  "id": 5,
                                  "name": "치즈 소스",
                                  "price": 1000,
                                  "is_selected": true
                                }
                              ]
                            }
                          ]
                        }
                        """
                )
            })
        ),
        @ApiResponse(responseCode = "401", description = "인증 정보 오류",
            content = @Content(mediaType = "application/json", examples = {
                @ExampleObject(name = "인증 정보 오류", summary = "인증 정보 오류", value = """
                    {
                      "code": "",
                      "message": "올바르지 않은 인증정보입니다.",
                      "errorTraceId": "5ba40351-6d27-40e5-90e3-80c5cf08a1ac"
                    }
                    """)
            })
        ),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 리소스",
            content = @Content(mediaType = "application/json", examples = {
                @ExampleObject(name = "장바구니 상품을 찾을 수 없는 경우", summary = "존재하지 않는 장바구니 상품", value = """
                    {
                      "code": "CART_MENU_ITEM_NOT_FOUND",
                      "message": "장바구니에 담긴 상품이 존재하지 않습니다",
                      "errorTraceId": "a1b2c3d4-e5f6-7890-1234-567890abcdef"
                    }
                    """)
            })
        )
    })
    @Operation(summary = "장바구니 옵션 변경용 메뉴 조회 API", description = """
        ### 장바구니 옵션 변경용 메뉴 조회
        - `is_selected` 필드가 포함된 메뉴 정보 조회 API
        """)
    @GetMapping("/cart/item/{cartMenuItemId}/edit")
    ResponseEntity<CartMenuItemEditResponse> getCartItemForEdit(
        @Parameter(description = "장바구니 상품 고유 ID", example = "1", required = true)
        @PathVariable Integer cartMenuItemId,

        @Parameter(hidden = true)
        Integer userId
    );
}
