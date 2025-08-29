package tech.desafiobtg.orderms.controller;

import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tech.desafiobtg.orderms.dto.response.ApiResponse;
import tech.desafiobtg.orderms.dto.response.OrderDetailResponse;
import tech.desafiobtg.orderms.dto.response.OrderResponse;
import tech.desafiobtg.orderms.dto.response.PaginationResponse;
import tech.desafiobtg.orderms.service.OrderService;

@RestController
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/customers/{customerId}/orders")
    public ResponseEntity<ApiResponse<OrderResponse>> listOrders(@PathVariable("customerId") Long customerId,
                                                                 @RequestParam(name = "page", defaultValue = "0") Integer page,
                                                                 @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {

        page = page != null ? page : 0;
        pageSize = pageSize != null ? pageSize : 10;

        var pageResponse = orderService.findAllbyCustomerId(customerId, PageRequest.of(page, pageSize));

        var totalOnOrders = orderService.findTotalOnOrdersBycustomerId(customerId); 

        return ResponseEntity.ok(new ApiResponse<>(
            Map.of("totalOnOrders", totalOnOrders),
            pageResponse.getContent(),
            PaginationResponse.fromPage(pageResponse)
        ));
    }

    @GetMapping("/orders/{orderId}") 
    public ResponseEntity<OrderDetailResponse> getOrderById(@PathVariable("orderId") Long orderId) {
        var orderDetail = orderService.findByOrderId(orderId);
        return ResponseEntity.ok(orderDetail);
    }

}
