package tech.desafiobtg.orderms.service;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;

import tech.desafiobtg.orderms.dto.OrderCreatedEventDto;
import tech.desafiobtg.orderms.dto.OrderItemEventDto;
import tech.desafiobtg.orderms.dto.response.OrderDetailResponse;
import tech.desafiobtg.orderms.dto.response.OrderResponse;
import tech.desafiobtg.orderms.entity.OrderEntity;
import tech.desafiobtg.orderms.entity.OrderItem;
import tech.desafiobtg.orderms.exception.CustomerNotFoundException;
import tech.desafiobtg.orderms.exception.OrderNotFoundException;
import tech.desafiobtg.orderms.repository.OrderRepository;

class OrderServiceTest {

    private OrderRepository orderRepository;
    private MongoTemplate mongoTemplate;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        mongoTemplate = mock(MongoTemplate.class);
        orderService = new OrderService(orderRepository, mongoTemplate);
    }

    @Test
    void save_shouldPersistOrderCorrectly() {
        var items = List.of(new OrderItemEventDto("Produto A", 2, new BigDecimal("10.00")));
        var event = new OrderCreatedEventDto(1L, 100L, items);

        orderService.save(event);

        ArgumentCaptor<OrderEntity> captor = ArgumentCaptor.forClass(OrderEntity.class);
        verify(orderRepository).save(captor.capture());

        OrderEntity saved = captor.getValue();
        assertThat(saved.getOrderId()).isEqualTo(1L);
        assertThat(saved.getCustomerId()).isEqualTo(100L);
        assertThat(saved.getItems()).hasSize(1);
        assertThat(saved.getTotalValue()).isEqualByComparingTo("20.00");
    }

    @Test
    void getOrderItems_shouldMapEventToOrderItems() {
        var items = List.of(new OrderItemEventDto("Produto A", 2, new BigDecimal("10.00")));
        var event = new OrderCreatedEventDto(1L, 100L, items);

        List<OrderItem> result = OrderService.getOrderItems(event);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProduct()).isEqualTo("Produto A");
        assertThat(result.get(0).getQuantity()).isEqualTo(2);
        assertThat(result.get(0).getPrice()).isEqualByComparingTo("10.00");
    }

    @Test
    void findAllbyCustomerId_shouldReturnOrders() {
        var entity = new OrderEntity();
        entity.setOrderId(1L);
        entity.setCustomerId(100L);
        entity.setTotalValue(new BigDecimal("30.00"));

        Page<OrderEntity> page = new PageImpl<>(List.of(entity));
        when(orderRepository.findAllByCustomerId(eq(100L), any())).thenReturn(page);

        Page<OrderResponse> result = orderService.findAllbyCustomerId(100L, PageRequest.of(0, 10));

        assertThat(result).hasSize(1);
        assertThat(result.getContent().get(0).customerId()).isEqualTo(100L);
    }

    @Test
    void findAllbyCustomerId_shouldThrowException_whenNoOrdersFound() {
        when(orderRepository.findAllByCustomerId(eq(999L), any())).thenReturn(Page.empty());

        assertThatThrownBy(() -> orderService.findAllbyCustomerId(999L, PageRequest.of(0, 10)))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessageContaining("Customer not found with id: 999");
    }

    @Test
    void getTotal_shouldSumCorrectly() {
        var items = List.of(
                new OrderItemEventDto("Produto A", 2, new BigDecimal("10.00")),
                new OrderItemEventDto("Produto B", 1, new BigDecimal("5.50"))
        );
        var event = new OrderCreatedEventDto(1L, 100L, items);

        BigDecimal total = orderService.getTotal(event);

        assertThat(total).isEqualByComparingTo("25.50");
    }

   @Test
    void findTotalOnOrdersBycustomerId_shouldReturnTotalValue() {
        // Simula o retorno do MongoTemplate
        Document doc = new Document("totalValue", new BigDecimal("99.99"));
        AggregationResults<Document> results = new AggregationResults<>(List.of(doc), new Document());

        when(mongoTemplate.aggregate(any(Aggregation.class), eq("tb_orders"), eq(Document.class)))
            .thenReturn(results);

        BigDecimal result = orderService.findTotalOnOrdersBycustomerId(100L);

        assertThat(result).isEqualByComparingTo("99.99");
    }


    @Test
    void findTotalOnOrdersBycustomerId_shouldThrowException_whenNoResult() {
        // Mock do iterator vazio
        Iterator<Document> iterator = mock(Iterator.class);
        when(iterator.hasNext()).thenReturn(false); // Isso fará o serviço lançar a exceção
        
        // Mock do AggregationResults
        AggregationResults<Document> results = mock(AggregationResults.class);
        when(results.iterator()).thenReturn(iterator);
        
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("tb_orders"), eq(Document.class)))
            .thenReturn(results);

        assertThatThrownBy(() -> orderService.findTotalOnOrdersBycustomerId(999L))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessageContaining("Customer not found with id: 999");
    }

    @Test
    void findByOrderId_shouldReturnOrderDetail() {
        var entity = new OrderEntity();
        entity.setOrderId(1L);
        entity.setCustomerId(100L);
        
        // Inicializa a lista de itens para evitar NullPointerException
        var orderItem = new OrderItem("Produto A", 2, new BigDecimal("10.00"));
        entity.setItems(List.of(orderItem));
        
        // Configura o totalValue também, se necessário
        entity.setTotalValue(new BigDecimal("20.00"));
        
        when(orderRepository.findById(1L)).thenReturn(Optional.of(entity));

        OrderDetailResponse result = orderService.findByOrderId(1L);

        assertThat(result.customerId()).isEqualTo(100L);
        assertThat(result.orderId()).isEqualTo(1L);
    }

    @Test
    void findByOrderId_shouldThrowException_whenNotFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.findByOrderId(999L))
                .isInstanceOf(OrderNotFoundException.class);
    }
}
