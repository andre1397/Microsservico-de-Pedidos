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

    public void save(OrderCreatedEventDto event){//event é o evento de criação de pedido que foi recebido do RabbitMQ, contendo os dados do pedido e dos itens do pedido recebidos na mensagem dpelo RabbitMQ
        //Aqui estamos recebendo o evento de criação de pedido através do json da mensagem recebida pelo RabbitMQ, convertendo os itens do json pra objetos java (usando os dtos) e salvando no banco de dados
        var entity = new OrderEntity();//entity é o objeto que sera salvo no MongoDB, que representa o pedido
        entity.setOrderId(event.orderCode());
        entity.setCustomerId(event.customerCode());
        entity.setItems(getOrderItems(event));//Pega os itens de dentro da Lista de OrderItemEventDto e transforma em uma List de OrderItem
        entity.setTotalValue(getTotal(event));//Pega o total do pedido a partir da lista de OrderItemEventDto

        orderRepository.save(entity);//Salva o pedido no banco de dados
    }

    public static List<OrderItem> getOrderItems(OrderCreatedEventDto event) {//o stream foi posto em outro método pro código ficar mais enxuto
        return event.items().stream()
                .map(i -> new OrderItem(i.product(), i.quantity(), i.price()))//Mapeia os itens contidos na lista no event para objetos OrderItem, que são os itens do pedido. Cria um objeto OrderItem para cada item do pedido, usando os dados do dto OrderItemEventDto
                .toList();//Cria a lista com os itens do pedido, convertendo a lista de OrderItemEventDto para uma lista de OrderItem
    }

    public Page<OrderResponse> findAllbyCustomerId(Long customerId, PageRequest pageRequest){//page eh uma interface que representa uma pagina de resultados, que eh usada para paginar os resultados da consulta, usando o metodo findAll() do repository, que retorna uma pagina de resultados
        var orders = orderRepository.findAllByCustomerId(customerId, pageRequest);//buscar as orders de acordo com o customerId e o pageRequest

        if (customerId == null) {
            throw new IllegalArgumentException("customerId cannot be null");
        }
        if (orders.isEmpty()) {
            throw new CustomerNotFoundException("Customer not found with id: " + customerId);
        }

        return orders.map(OrderResponse::fromEntity);//Mapeia os OrderEntity para OrderResponse
    }

    public BigDecimal getTotal(OrderCreatedEventDto event){
        return event.items().stream()
                .map(i -> i.price().multiply(BigDecimal.valueOf(i.quantity()))) //Mapeia pro stream poder retornar um BigDecimal e usa o método multiply() pra multiplicar o preco pela quantidade de itens do mesmo tipo informado no campo quantity na lista
                .reduce(BigDecimal::add) //BigDecimal::add eh usado para somar os valores da stream, reduceeh pra transformar um um unico valor no final
                .orElse(BigDecimal.ZERO); //Se a stream for vazia, retorna 0
    }

    public BigDecimal findTotalOnOrdersBycustomerId(Long customerId){
        if (customerId == null) {
            throw new IllegalArgumentException("customerId cannot be null");
        }

         var aggregations = newAggregation( //Cria uma agregação para calcular o total de pedidos do cliente
            match(Criteria.where("customerId").is(customerId)), 
            group().sum("totalValue").as("totalValue") 
        );

        var response = mongoTemplate.aggregate(aggregations, "tb_orders", Document.class); //Executa a agregação e retorna o resultado, que eh um Document
        if (!response.iterator().hasNext()) {
            throw new CustomerNotFoundException("Customer not found with id: " + customerId);
        }

        return new BigDecimal(response.getUniqueMappedResult().get("totalValue").toString()); //Pega o resultado da agregação, que é um Document, e converte o campo total para BigDecimal
    }

    public OrderDetailResponse findByOrderId(Long orderId) {
    var order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));

    return OrderDetailResponse.fromEntity(order);
}

}
