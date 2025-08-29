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

    @GetMapping("/customers/{customerId}/orders") //Chama as orders de acordo com o cliente identificado pelo customerId
    public ResponseEntity<ApiResponse<OrderResponse>> listOrders(@PathVariable("customerId") Long customerId, //PathVariable vai dentro da url, nesse caso = http://localhost:8080/customers/{customerId}/orders ou http://localhost:8080/customers/1/orders?page={page}&pageSize={pageSize}
                                                                 @RequestParam(name = "page", defaultValue = "0") Integer page,//RequestParam vai depois do ? na url da requisicao
                                                                 @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {

        page = page != null ? page : 0;
        pageSize = pageSize != null ? pageSize : 10;

        var pageResponse = orderService.findAllbyCustomerId(customerId, PageRequest.of(page, pageSize));

        var totalOnOrders = orderService.findTotalOnOrdersBycustomerId(customerId); 

        return ResponseEntity.ok(new ApiResponse<>(
            Map.of("totalOnOrders", totalOnOrders),//Map.of cria um mapa com o total de pedidos do cliente, que sera usado no summary da resposta
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
