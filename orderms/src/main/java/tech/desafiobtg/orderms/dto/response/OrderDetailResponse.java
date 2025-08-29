package tech.desafiobtg.orderms.dto.response;

import java.math.BigDecimal;
import java.util.List;

import tech.desafiobtg.orderms.entity.OrderEntity;
import tech.desafiobtg.orderms.entity.OrderItem;

public record OrderDetailResponse(Long orderId,
                                  Long customerId,
                                  BigDecimal totalValue,
                                  List<OrderItemResponse> items) {

    public static OrderDetailResponse fromEntity(OrderEntity entity) {
        return new OrderDetailResponse(
                entity.getOrderId(),
                entity.getCustomerId(),
                entity.getTotalValue(),
                entity.getItems().stream().map(OrderItemResponse::fromEntity).toList()
        );
    }

    public record OrderItemResponse(String product,
                                    Integer quantity,
                                    BigDecimal price) {
                                        
        public static OrderItemResponse fromEntity(OrderItem item) {
            return new OrderItemResponse(item.getProduct(), item.getQuantity(), item.getPrice());
        }
    }
}