package tech.desafiobtg.orderms.dto;

import java.math.BigDecimal;

public record OrderItemEventDto (String product, 
                                 Integer quantity,
                                 BigDecimal price) {
}
