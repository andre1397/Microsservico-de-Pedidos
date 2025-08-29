package tech.desafiobtg.orderms.dto;

import java.util.List;

public record OrderCreatedEventDto(Long orderCode, 
                                   Long customerCode, 
                                   List<OrderItemEventDto> items) {
}
