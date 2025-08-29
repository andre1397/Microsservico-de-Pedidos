package tech.desafiobtg.orderms.dto;

import java.util.List;

//Classe que representa o evento de criação de pedido, que será enviado para a fila do RabbitMQ

//Os nomes dos atributos devem ser iguais aos nomes dos atributos da classe OrderCreatedEvent e iguais aos nomes dos atributos do JSON que será enviado para a fila do RabbitMQ
public record OrderCreatedEventDto(Long orderCode, 
                                   Long customerCode, 
                                   List<OrderItemEventDto> items) {
}
