package in.koreatech.koin.domain.order.shop.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import in.koreatech.koin.domain.order.shop.dto.event.OrderableShopEventsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "(Normal) OrderShopEvent: 주문 가능 상점 이벤트", description = "주문 가능 상점 이벤트를 관리한다")
public interface OrderableShopEventApi {

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "특정 주문 가능 상점 이벤트 조회 성공",
                content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "해당 상점 이벤트 존재", value = """
                            {
                              "shop_events": [
                                {
                                  "orderable_shop_id": 1,
                                  "shop_id": 1,
                                  "shop_name": "술꾼",
                                  "event_id": 1,
                                  "title": "콩순이 사장님이 미쳤어요!!",
                                  "content": "콩순이 가게 전메뉴 90% 할인! 가게 폐업 임박...",
                                  "thumbnail_images": [
                                    "https://static.koreatech.in/example.png"
                                  ],
                                  "start_date": "2024-10-22",
                                  "end_date": "2024-10-25"
                                }
                              ]
                            }
                        """
                    ),
                    @ExampleObject(name = "해당 상점 이벤트 없음", value = """
                            {
                              "shop_events": []
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
                          "errorTraceId": "b630af74-f0e5-4faf-808f-5406ab104848"
                        }
                        """
                    )
                })
            )
        }
    )
    @Operation(summary = "특정 주문 가능 상점의 모든 이벤트 조회")
    @GetMapping("/order/shop/{orderableShopId}/events")
    ResponseEntity<OrderableShopEventsResponse> getShopEvents(
        @PathVariable Integer orderableShopId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "모든 주문 가능 상점의 모든 이벤트 조회 성공",
                content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "성공", value = """
                            {
                              "shop_events": [
                                {
                                  "orderable_shop_id": 1,
                                  "shop_id": 1,
                                  "shop_name": "술꾼",
                                  "event_id": 1,
                                  "title": "콩순이 사장님이 미쳤어요!!",
                                  "content": "콩순이 가게 전메뉴 90% 할인! 가게 폐업 임박...",
                                  "thumbnail_images": [
                                    "https://static.koreatech.in/example.png"
                                  ],
                                  "start_date": "2024-10-22",
                                  "end_date": "2024-10-25"
                                }
                              ]
                            }
                        """
                    )
                })
            )
        }
    )
    @Operation(summary = "모든 주문 가능 상점의 모든 이벤트 조회")
    @GetMapping("/order/shops/events")
    ResponseEntity<OrderableShopEventsResponse> getShopAllEvent();
}
