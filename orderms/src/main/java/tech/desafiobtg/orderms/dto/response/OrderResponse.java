package tech.desafiobtg.orderms.dto.response;

import java.math.BigDecimal;

import tech.desafiobtg.orderms.entity.OrderEntity;

public record OrderResponse(Long orderId,
                            Long customerId,
                            BigDecimal totalValue) {

    public static OrderResponse fromEntity(OrderEntity orderEntity) {
        return new OrderResponse(orderEntity.getOrderId(), orderEntity.getCustomerId(), orderEntity.getTotalValue());//converte o OrderEntity para OrderResponse
    }

}
