package tech.desafiobtg.orderms.controller;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import tech.desafiobtg.orderms.dto.response.ApiResponse;
import tech.desafiobtg.orderms.dto.response.OrderDetailResponse;
import tech.desafiobtg.orderms.dto.response.OrderResponse;
import tech.desafiobtg.orderms.dto.response.PaginationResponse;
import tech.desafiobtg.orderms.service.OrderService;

class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void listOrders_shouldReturnOrdersWithPagination() {
        
        Long customerId = 100L;
        int page = 0;
        int pageSize = 10;
        
        OrderResponse orderResponse = new OrderResponse(1L, 100L, new BigDecimal("50.00"));
        Page<OrderResponse> pageResponse = new PageImpl<>(List.of(orderResponse));
        
        when(orderService.findAllbyCustomerId(eq(customerId), any(PageRequest.class)))
            .thenReturn(pageResponse);
        when(orderService.findTotalOnOrdersBycustomerId(customerId))
            .thenReturn(new BigDecimal("150.00"));

        
        ResponseEntity<ApiResponse<OrderResponse>> response = 
            orderController.listOrders(customerId, page, pageSize);

        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        
        ApiResponse<OrderResponse> apiResponse = response.getBody();
        assertThat(apiResponse.summary()).containsKey("totalOnOrders");
        assertThat(apiResponse.summary().get("totalOnOrders")).isEqualTo(new BigDecimal("150.00"));
        assertThat(apiResponse.data()).hasSize(1);
        assertThat(apiResponse.data().get(0).customerId()).isEqualTo(100L);
        assertThat(apiResponse.pagination()).isNotNull();
    }

    @Test
    void listOrders_shouldUseDefaultPaginationValues() {
        
        Long customerId = 100L;
        OrderResponse orderResponse = new OrderResponse(1L, 100L, new BigDecimal("50.00"));
        Page<OrderResponse> pageResponse = new PageImpl<>(List.of(orderResponse));
        
        when(orderService.findAllbyCustomerId(eq(customerId), any(PageRequest.class)))
            .thenReturn(pageResponse);
        when(orderService.findTotalOnOrdersBycustomerId(customerId))
            .thenReturn(new BigDecimal("150.00"));

        
        ResponseEntity<ApiResponse<OrderResponse>> response = 
            orderController.listOrders(customerId, null, null);

        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().data()).hasSize(1);
    }

    @Test
    void getOrderById_shouldReturnOrderDetail() {
        
        Long orderId = 1L;
        OrderDetailResponse orderDetail = new OrderDetailResponse(
            1L, 100L, new BigDecimal("50.00"), List.of()
        );
        
        when(orderService.findByOrderId(orderId)).thenReturn(orderDetail);

        
        ResponseEntity<OrderDetailResponse> response = orderController.getOrderById(orderId);

        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().orderId()).isEqualTo(1L);
        assertThat(response.getBody().customerId()).isEqualTo(100L);
    }

    @Test
    void getOrderById_shouldReturnCorrectOrder() {
        
        Long orderId = 1L;
        OrderDetailResponse expectedDetail = new OrderDetailResponse(
            1L, 100L, new BigDecimal("75.50"), List.of()
        );
        
        when(orderService.findByOrderId(orderId)).thenReturn(expectedDetail);

        
        ResponseEntity<OrderDetailResponse> response = orderController.getOrderById(orderId);

        
        assertThat(response.getBody()).isEqualTo(expectedDetail);
    }

    @Test
    void listOrders_shouldReturnCorrectPaginationInfo() {
        
        Long customerId = 100L;
        OrderResponse order1 = new OrderResponse(1L, 100L, new BigDecimal("50.00"));
        OrderResponse order2 = new OrderResponse(2L, 100L, new BigDecimal("75.00"));
        Page<OrderResponse> pageResponse = new PageImpl<>(List.of(order1, order2), PageRequest.of(0, 10), 20);
        
        when(orderService.findAllbyCustomerId(eq(customerId), any(PageRequest.class)))
            .thenReturn(pageResponse);
        when(orderService.findTotalOnOrdersBycustomerId(customerId))
            .thenReturn(new BigDecimal("125.00"));

        
        ResponseEntity<ApiResponse<OrderResponse>> response = 
            orderController.listOrders(customerId, 0, 10);

        
        assertThat(response.getBody().pagination()).isNotNull();
        PaginationResponse pagination = response.getBody().pagination();
        assertThat(pagination.page()).isEqualTo(0);
        assertThat(pagination.pageSize()).isEqualTo(10);
        assertThat(pagination.totalElements()).isEqualTo(20);
        assertThat(pagination.totalPages()).isEqualTo(2);
    }
}