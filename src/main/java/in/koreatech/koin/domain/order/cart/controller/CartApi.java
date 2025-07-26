package in.koreatech.koin.domain.order.cart.controller;

import static in.koreatech.koin.domain.user.model.UserType.GENERAL;
import static in.koreatech.koin.domain.user.model.UserType.STUDENT;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import in.koreatech.koin._common.auth.Auth;
import in.koreatech.koin._common.duplicate.DuplicateGuard;
import in.koreatech.koin.domain.order.cart.dto.CartAddItemRequest;
import in.koreatech.koin.domain.order.cart.dto.CartAmountSummaryResponse;
import in.koreatech.koin.domain.order.cart.dto.CartPaymentSummaryResponse;
import in.koreatech.koin.domain.order.cart.dto.CartResponse;
import in.koreatech.koin.domain.order.cart.dto.CartMenuItemEditResponse;
import in.koreatech.koin.domain.order.cart.dto.CartUpdateItemRequest;
import in.koreatech.koin.domain.order.model.OrderType;
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
        @ApiResponse(responseCode = "200", description = "장바구니 조회 성공",
            content = @Content(mediaType = "application/json", examples = {
                @ExampleObject(name = "장바구니에 상품 존재", value = """
                    {
                      "shop_name": "굿모닝살로만치킨",
                      "shop_thumbnail_image_url": "https://static.koreatech.in/test.png",
                      "orderable_shop_id": 2,
                      "is_delivery_available": true,
                      "is_takeout_available": true,
                      "shop_minimum_order_amount": 15000,
                      "items": [
                        {
                          "cart_menu_item_id": 12,
                          "orderable_shop_menu_id": 3,
                          "name": "허니콤보",
                          "menu_thumbnail_image_url": "https://static.koreatech.in/test.png",
                          "quantity": 1,
                          "total_amount": 23000,
                          "price": {
                            "name": null,
                            "price": 23000
                          },
                          "options": [],
                          "is_modified": false
                        },
                        {
                          "cart_menu_item_id": 13,
                          "orderable_shop_menu_id": 4,
                          "name": "레드콤보",
                          "menu_thumbnail_image_url": "https://static.koreatech.in/test.png",
                          "quantity": 2,
                          "total_amount": 47000,
                          "price": {
                            "name": "뼈",
                            "price": 23000
                          },
                          "options": [
                            {
                              "option_group_name": "소스 추가",
                              "option_name": "레드디핑 소스",
                              "option_price": 500
                            }
                          ],
                          "is_modified": false
                        }
                      ],
                      "items_amount": 70000,
                      "delivery_fee": 0,
                      "total_amount": 70000,
                      "final_payment_amount": 70000
                    }
                    """),
                @ExampleObject(name = "장바구니에 상품 없음", value = """
                    {
                      "shop_name": null,
                      "shop_thumbnail_image_url": null,
                      "orderable_shop_id": null,
                      "is_delivery_available": false,
                      "is_takeout_available": false,
                      "shop_minimum_order_amount": 0,
                      "items": [],
                      "items_amount": 0,
                      "delivery_fee": 0,
                      "total_amount": 0,
                      "final_payment_amount": 0
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
        @ApiResponse(responseCode = "400", description = "잘못된 요청",
            content = @Content(mediaType = "application/json", examples = {
                @ExampleObject(name = "배달 가능한 상점이 아닌 경우", summary = "배달 가능한 상점이 아닙니다", value = """
                    {
                      "code": "SHOP_NOT_DELIVERABLE",
                      "message": "배달 가능한 상점이 아닙니다.",
                      "errorTraceId": "a1b2c3d4-e5f6-7890-1234-567890abcdef"
                    }
                    """),
                @ExampleObject(name = "포장 가능한 상점이 아닌 경우", summary = "포당 가능한 상점이 아닙니다", value = """
                    {
                      "code": "SHOP_NOT_TAKEOUT_AVAILABLE",
                      "message": "포장 가능한 상점이 아닙니다.",
                      "errorTraceId": "a1b2c3d4-e5f6-7890-1234-567890abcdef"
                    }
                    """)
            })
        ),
    })
    @Operation(summary = "장바구니 조회", description = """
        ### 사용자의 장바구니 전체 정보 조회
        - 장바구니에 담긴 모든 상품, 금액, 배달비 정보 등을 포함하여 반환합니다.
        
        ### 응답 케이스
        - **장바구니에 상품이 있는 경우**: 장바구니 상세 정보 응답을 반환합니다.
        - **장바구니가 비어있는 경우**: 필드가 null로 비어있는 응답을 반환합니다.
        
        ### nullable
        - **shop_thumbnail_image_url** : 주문 가능 상점의 썸네일 이미지가 존재하지 않는 경우
        - **items[i].menu_thumbnail_image_url** : 주문 가능 상점의 메뉴 썸네일 이미지가 존재하지 않는 경우
        - **items[i].price.name** : 주문 가능 상점 메뉴의 가격 옵션 이름이 없는 경우 (단일 가격)
        """)
    @GetMapping("/cart")
    ResponseEntity<CartResponse> getCartItems(
        @Parameter(hidden = true)
        Integer userId,
        @RequestParam(name = "type") OrderType type
    );

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
                    """),
                @ExampleObject(name = "상점의 영업시간이 아님", summary = "상점의 영업시간이 아님", value = """
                    {
                      "code": "SHOP_CLOSED",
                      "message": "상점의 영업시간이 아닙니다.",
                      "errorTraceId": "ae4feff5-5f37-4f91-b8b6-a5957fd5bb10"
                    }
                    """),
                @ExampleObject(name = "추가 수량 1 미만", summary = "추가 수량 1 미만", value = """
                    {
                      "code": "INVALID_REQUEST_BODY",
                      "message": "수량은 최소 1 입니다.",
                      "errorTraceId": "b5327d2e-77a4-4d76-88f0-fc3e6f829512",
                      "fieldErrors": [
                        {
                          "field": "quantity",
                          "message": "수량은 최소 1 입니다.",
                          "constraint": "Min"
                        }
                      ]
                    }
                    """),
                @ExampleObject(name = "추가 수량 10 초과", summary = "추가 수량 10 초과", value = """
                    {
                      "code": "INVALID_REQUEST_BODY",
                      "message": "수량은 최대 10 입니다.",
                      "errorTraceId": "13780c0b-b15c-459c-b854-fcdca7e68489",
                      "fieldErrors": [
                        {
                          "field": "quantity",
                          "message": "수량은 최대 10 입니다.",
                          "constraint": "Max"
                        }
                      ]
                    }
                    """),
                @ExampleObject(name = "추가 수량 null", summary = "추가 수량 null", value = """
                    {
                      "code": "INVALID_REQUEST_BODY",
                      "message": "quantity는 필수값입니다.",
                      "errorTraceId": "5969c702-ab15-40d3-b71a-6dec2a16a4c1",
                      "fieldErrors": [
                        {
                          "field": "quantity",
                          "message": "quantity는 필수값입니다.",
                          "constraint": "NotNull"
                        }
                      ]
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
                      "code": "NOT_FOUND_ORDERABLE_SHOP_MENU_PRICE",
                      "message": "유효하지 않은 가격 ID 입니다.",
                      "errorTraceId": "a1b2c3d4-e5f6-7890-1234-567890abcdef"
                    }
                    """),
                @ExampleObject(name = "유효하지 않은 메뉴 옵션 ID", summary = "메뉴 옵션 ID 없음", value = """
                    {
                      "code": "NOT_FOUND_ORDERABLE_SHOP_MENU_OPTION",
                      "message": "유효하지 않은 옵션 ID 입니다.",
                      "errorTraceId": "a1b2c3d4-e5f6-7890-1234-567890abcdef"
                    }
                    """)
            })
        ),
        @ApiResponse(responseCode = "409", description = "중복 요청",
            content = @Content(mediaType = "application/json", examples = {
                @ExampleObject(name = "중복 요청(따닥) 발생", summary = "중복 요청 발생", value = """
                    {
                        "code": "REQUEST_TOO_FAST",
                        "message": "요청이 너무 빠릅니다. 다시 요청해주세요.",
                        "errorTraceId": "94920181-d210-4252-8092-860bd002651d"
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
        - **quantity**: 추가 수량 (null, 0 이하의 값, 11 이상의 값을 허용하지 않습니다.)
        
        ### 중복 요청 처리
        - 0.1초 이내에 같은 요청이 도착한 경우, 둘 중 하나는 중복 요청으로 판단하여 409 에러가 반환됩니다.
        """)
    @DuplicateGuard(key = "#userId + ':' + #cartAddItemRequest.toString()")
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
                      "code": "INVALID_CART_ITEM_QUANTITY",
                      "message": "유효하지 않은 수량입니다.",
                      "errorTraceId": "a1b2c3d4-e5f6-7890-1234-567890abcdef"
                    }
                    """),
                @ExampleObject(name = "수량이 null인 경우", summary = "수량 null", value = """
                    {
                      "code": "INVALID_CART_ITEM_QUANTITY",
                      "message": "유효하지 않은 수량입니다.",
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
                      "code": "NOT_FOUND_CART_ITEM",
                      "message": "장바구니에 담긴 상품이 존재하지 않습니다",
                      "errorTraceId": "a1b2c3d4-e5f6-7890-1234-567890abcdef"
                    }
                    """),
                @ExampleObject(name = "장바구니가 없는 경우", summary = "장바구니 없음", value = """
                    {
                      "code": "NOT_FOUND_CART",
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
                      "code": "NOT_FOUND_CART_ITEM",
                      "message": "장바구니에 담긴 상품이 존재하지 않습니다",
                      "errorTraceId": "a1b2c3d4-e5f6-7890-1234-567890abcdef"
                    }
                    """),
                @ExampleObject(name = "장바구니가 없는 경우", summary = "장바구니 없음", value = """
                    {
                      "code": "NOT_FOUND_CART",
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
                      "code": "NOT_FOUND_CART",
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
                      "code": "NOT_FOUND_CART_ITEM",
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

    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "장바구니 상품 가격/옵션 변경 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청",
            content = @Content(mediaType = "application/json", examples = {
                @ExampleObject(name = "필수 옵션을 선택하지 않은 경우", summary = "필수 옵션 누락", value = """
                    {
                      "code": "REQUIRED_OPTION_GROUP_MISSING",
                      "message": "필수 옵션 그룹을 선택하지 않았습니다.",
                      "errorTraceId": "a1b2c3d4-e5f6-7890-1234-567890abcdef"
                    }
                    """),
                @ExampleObject(name = "옵션 최소 선택 개수를 만족하지 못한 경우", summary = "옵션 최소 선택 미만", value = """
                    {
                      "code": "MIN_SELECTION_NOT_MET",
                      "message": "옵션 그룹의 최소 선택 개수를 만족하지 못했습니다.",
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
                @ExampleObject(name = "선택한 옵션이 해당 옵션 그룹에 속해 있지 않은 경우", summary = "옵션-그룹 불일치", value = """
                    {
                      "code": "INVALID_OPTION_IN_GROUP",
                      "message": "선택한 옵션이 해당 옵션 그룹에 속해있지 않습니다.",
                      "errorTraceId": "a1b2c3d4-e5f6-7890-1234-567890abcdef"
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
                @ExampleObject(name = "장바구니 상품을 찾을 수 없는 경우", summary = "존재하지 않는 장바구니 상품", value = """
                    {
                      "code": "NOT_FOUND_CART_ITEM",
                      "message": "장바구니에 담긴 상품이 존재하지 않습니다",
                      "errorTraceId": "a1b2c3d4-e5f6-7890-1234-567890abcdef"
                    }
                    """),
                @ExampleObject(name = "유효하지 않은 메뉴 가격 ID", summary = "메뉴 가격 ID 없음", value = """
                    {
                      "code": "NOT_FOUND_ORDERABLE_SHOP_MENU_PRICE",
                      "message": "유효하지 않은 가격 ID 입니다.",
                      "errorTraceId": "a1b2c3d4-e5f6-7890-1234-567890abcdef"
                    }
                    """),
                @ExampleObject(name = "유효하지 않은 메뉴 옵션 ID", summary = "메뉴 옵션 ID 없음", value = """
                    {
                      "code": "NOT_FOUND_ORDERABLE_SHOP_MENU_OPTION",
                      "message": "유효하지 않은 옵션 ID 입니다.",
                      "errorTraceId": "a1b2c3d4-e5f6-7890-1234-567890abcdef"
                    }
                    """)
            })
        )
    })
    @Operation(summary = "장바구니 상품 가격/옵션 변경", description = """
        ### 장바구니 상품의 가격 및 옵션 구성 변경
        - 장바구니에 담긴 특정 상품(`cartMenuItemId`)의 가격 및 옵션 구성을 변경합니다.
        
        ### 동작 방식
        1.  **유효성 검증**: 요청된 가격과 옵션들이 메뉴에 유효한지 검증합니다.
        2.  **중복 확인**: 변경된 구성(가격+옵션)이 장바구니 내 다른 상품과 동일한지 확인합니다.
        3.  **병합 또는 수정**:
            - **동일 구성 상품이 존재하면**: 해당 상품의 수량을 늘리고, 현재 수정 중인 상품은 삭제합니다 (병합).
            - **동일 구성 상품이 없으면**: 현재 상품의 가격과 옵션을 요청된 내용으로 수정합니다.
            
        ### 요청 Body 필드 설명
        - **orderableShopMenuPriceId**: 새롭게 선택한 메뉴 가격 ID (필수)
        - **options**: 새롭게 선택한 옵션 목록 (선택 사항)
          - **optionGroupId**: 옵션 그룹 ID
          - **optionId**: 옵션 ID
        """)
    @PutMapping("/cart/item/{cartMenuItemId}")
    ResponseEntity<Void> updateCartItem(
        @Parameter(description = "가격을 변경할 장바구니 상품의 고유 ID", example = "1", required = true)
        @PathVariable Integer cartMenuItemId,
        @RequestBody @Valid CartUpdateItemRequest request,
        @Parameter(hidden = true)
        Integer userId
    );

    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "상점 페이지 하단 Bottom Sheet 장바구니 정보 조회 성공",
            content = @Content(mediaType = "application/json", examples = {
                @ExampleObject(name = "장바구니에 상품 존재, 요청 상점 ID와 장바구니에 담긴 상점 ID 일치", value = """
                        {
                          "orderable_shop_id": 101,
                          "shop_minimum_order_amount": 20000,
                          "cart_items_amount": 17500,
                          "is_available": true
                        }
                        """
                ),
                @ExampleObject(name = "장바구니 비어있음, 요청 상점 ID와 장바구니에 담긴 상점 ID 불일치", value = """
                        {
                          "orderable_shop_id": 0,
                          "shop_minimum_order_amount": 0,
                          "cart_items_amount": 0,
                          "is_available": false
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
        )
    })
    @Operation(summary = "상점 페이지 하단 Bottom Sheet 장바구니 정보 조회", description = """
        ### 상점 페이지 하는 Bottom Sheet
        - 장바구니에는 동일한 상점(orderableShopId)의 메뉴만 담을 수 있습니다.
        - 요청한 상점 ID(orderableShopId)가 장바구니에 담긴 메뉴의 상점 ID와 일치하는 경우에만 장바구니 정보를 반환합니다.
        - 일치하지 않거나 장바구니가 비어 있는 경우, `is_available` 필드가 false로 반환됩니다. (api 출력 예시 참조)
        """)
    @GetMapping("/cart/summary/{orderableShopId}")
    ResponseEntity<CartAmountSummaryResponse> getCartSummaryForBottomSheet(
        @Parameter(description = "주문 가능 상점 고유 식별자", example = "1", required = true)
        @PathVariable Integer orderableShopId,
        @Parameter(hidden = true)
        Integer userId
    );

    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "장바구니 결제 금액 정보 조회 성공",
            content = @Content(mediaType = "application/json", examples = {
                @ExampleObject(name = "장바구니가 유효한 경우", value = """
                        {
                          "item_total_amount": 20000,
                          "delivery_fee": 1500,
                          "total_amount": 21500,
                          "final_payment_amount": 21500
                        }
                        """
                ),
                @ExampleObject(name = "장바구니 비어있는 경우", value = """
                        {
                          "item_total_amount": 0,
                          "delivery_fee": 0,
                          "total_amount": 0,
                          "final_payment_amount": 0
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
        @ApiResponse(responseCode = "400", description = "잘못된 요청",
            content = @Content(mediaType = "application/json", examples = {
                @ExampleObject(name = "배달 가능한 상점이 아닌 경우", summary = "배달 가능한 상점이 아닙니다", value = """
                    {
                      "code": "SHOP_NOT_DELIVERABLE",
                      "message": "배달 가능한 상점이 아닙니다.",
                      "errorTraceId": "a1b2c3d4-e5f6-7890-1234-567890abcdef"
                    }
                    """),
                @ExampleObject(name = "포장 가능한 상점이 아닌 경우", summary = "포당 가능한 상점이 아닙니다", value = """
                    {
                      "code": "SHOP_NOT_TAKEOUT_AVAILABLE",
                      "message": "포장 가능한 상점이 아닙니다.",
                      "errorTraceId": "a1b2c3d4-e5f6-7890-1234-567890abcdef"
                    }
                    """)
            })
        )
    })
    @Operation(
        summary = "장바구니 결제 금액 정보 조회",
        description = """
            장바구니에 담긴 전체 상품 금액, 배달비, 결제 예정 금액을 포함한 결제 요약 정보를 조회합니다.
            - 장바구니가 비어 있거나 유효하지 않은 경우, 빈 응답을 반환합니다.
            """
    )
    @GetMapping("/cart/payment/summary")
    ResponseEntity<CartPaymentSummaryResponse> getCartPaymentSummary(
        @Parameter(hidden = true) @Auth(permit = {GENERAL, STUDENT}) Integer userId,
        @RequestParam(name = "type") OrderType type
    );

    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "장바구니 검증 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청",
            content = @Content(mediaType = "application/json", examples = {
                @ExampleObject(name = "최소 주문 금액 미충족", summary = "최소 주문 금액 미충족", value = """
                    {
                      "code": "ORDER_AMOUNT_BELOW_MINIMUM",
                      "message": "최소 주문 금액을 충족하지 않습니다.",
                      "errorTraceId": "a1b2c3d4-e5f6-7890-1234-567890abcdef"
                    }
                    """),
                @ExampleObject(name = "상점의 영업시간이 아님", summary = "상점의 영업시간이 아님", value = """
                    {
                      "code": "SHOP_CLOSED",
                      "message": "상점의 영업시간이 아닙니다.",
                      "errorTraceId": "ae4feff5-5f37-4f91-b8b6-a5957fd5bb10"
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
                @ExampleObject(name = "장바구니가 없는 경우", summary = "장바구니 없음", value = """
                    {
                      "code": "NOT_FOUND_CART",
                      "message": "장바구니를 찾을 수 없습니다.",
                      "errorTraceId": "a1b2c3d4-e5f6-7890-1234-567890abcdef"
                    }
                    """)
            })
        )
    })
    @Operation(
        summary = "장바구니 검증",
        description = """
            ## 장바구니 검증
            - 장바구니의 상품 주문 금액이 상점의 최소 주문 금액을 충족하는지 검증합니다.
            - 상점이 현재 주문 가능 상태(영업 중) 인지 검증합니다.
            """
    )
    @GetMapping("/cart/validate")
    ResponseEntity<Void> getCartValidateResult(
        @Parameter(hidden = true) @Auth(permit = {GENERAL, STUDENT}) Integer userId
    );
}
