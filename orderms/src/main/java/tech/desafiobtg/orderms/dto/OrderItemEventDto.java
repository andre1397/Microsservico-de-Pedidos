package tech.desafiobtg.orderms.dto;

import java.math.BigDecimal;

//Classe que representa um item do pedido no evento de criação de pedido
//Será usado para enviar os dados do item do pedido para a fila do RabbitMQ

//Os nomes dos atributos devem ser iguais aos nomes dos atributos da classe OrderItemEvent e iguais aos nomes dos atributos do JSON que será enviado para a fila do RabbitMQ
public record OrderItemEventDto (String product, 
                                 Integer quantity,
                                 BigDecimal price) {
}
