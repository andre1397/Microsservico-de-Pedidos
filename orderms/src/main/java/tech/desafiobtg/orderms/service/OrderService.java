package tech.desafiobtg.orderms.service;

import java.math.BigDecimal;
import java.util.List;

import org.bson.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import tech.desafiobtg.orderms.dto.OrderCreatedEventDto;
import tech.desafiobtg.orderms.dto.response.OrderDetailResponse;
import tech.desafiobtg.orderms.dto.response.OrderResponse;
import tech.desafiobtg.orderms.entity.OrderEntity;
import tech.desafiobtg.orderms.entity.OrderItem;
import tech.desafiobtg.orderms.exception.CustomerNotFoundException;
import tech.desafiobtg.orderms.exception.OrderNotFoundException;
import tech.desafiobtg.orderms.repository.OrderRepository;

@Service
public class OrderService {
    private final OrderRepository orderRepository;

    private final MongoTemplate mongoTemplate;

    public OrderService(OrderRepository orderRepository, MongoTemplate mongoTemplate) {
        this.orderRepository = orderRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public void save(OrderCreatedEventDto event){
        
        var entity = new OrderEntity();
        entity.setOrderId(event.orderCode());
        entity.setCustomerId(event.customerCode());
        entity.setItems(getOrderItems(event));
        entity.setTotalValue(getTotal(event));

        orderRepository.save(entity);
    }

    public static List<OrderItem> getOrderItems(OrderCreatedEventDto event) {
        return event.items().stream()
                .map(i -> new OrderItem(i.product(), i.quantity(), i.price()))
                .toList();
    }

    public Page<OrderResponse> findAllbyCustomerId(Long customerId, PageRequest pageRequest){
        var orders = orderRepository.findAllByCustomerId(customerId, pageRequest);

        if (customerId == null) {
            throw new IllegalArgumentException("customerId cannot be null");
        }
        if (orders.isEmpty()) {
            throw new CustomerNotFoundException("Customer not found with id: " + customerId);
        }

        return orders.map(OrderResponse::fromEntity);
    }

    public BigDecimal getTotal(OrderCreatedEventDto event){
        return event.items().stream()
                .map(i -> i.price().multiply(BigDecimal.valueOf(i.quantity()))) 
                .reduce(BigDecimal::add) 
                .orElse(BigDecimal.ZERO);
    }

    public BigDecimal findTotalOnOrdersBycustomerId(Long customerId){
        if (customerId == null) {
            throw new IllegalArgumentException("customerId cannot be null");
        }

         var aggregations = newAggregation(
            match(Criteria.where("customerId").is(customerId)), 
            group().sum("totalValue").as("totalValue") 
        );

        var response = mongoTemplate.aggregate(aggregations, "tb_orders", Document.class);
        if (!response.iterator().hasNext()) {
            throw new CustomerNotFoundException("Customer not found with id: " + customerId);
        }

        return new BigDecimal(response.getUniqueMappedResult().get("totalValue").toString());
    }

    public OrderDetailResponse findByOrderId(Long orderId) {
    var order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));

    return OrderDetailResponse.fromEntity(order);
}

}
