package tech.desafiobtg.orderms.listener;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import com.rabbitmq.client.Channel;

import static tech.desafiobtg.orderms.config.RabbitMqConfig.ORDER_CREATED_QUEUE;
import tech.desafiobtg.orderms.dto.OrderCreatedEventDto;
import tech.desafiobtg.orderms.exception.OrderProcessingException;
import tech.desafiobtg.orderms.service.OrderService;

@Component // Anotação para indicar que esta classe é um componente do Spring, permitindo que seja detectada e gerenciada pelo contexto do Spring
// A anotação @Component é usada para registrar a classe como um bean no contexto do Spring. Um bean é um objeto que é instanciado, montado e gerenciado pelo contêiner do Spring. Isso permite que o Spring cuide do ciclo de vida do objeto, injeção de dependências e outras funcionalidades relacionadas ao gerenciamento de beans.
// Isso é útil para classes que precisam ser detectadas automaticamente pelo Spring, como listeners, serviços
public class OrderCreatedListener {

    private final Logger logger = LoggerFactory.getLogger(OrderCreatedListener.class); // Cria um logger para registrar mensagens de log, útil para depuração e monitoramento

    private final OrderService orderService;

    public OrderCreatedListener(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Listener para a fila de pedidos criados.
     * Usa Message<OrderCreatedEventDto> para capturar o payload e headers da mensagem.
     * ackMode="MANUAL" permite confirmar manualmente o recebimento.
     */
    @RabbitListener(queues = ORDER_CREATED_QUEUE, ackMode = "MANUAL")// Anotação para indicar que este método deve ser chamado quando uma mensagem for recebida na fila especificada. A fila é definida na constante ORDER_CREATED_QUEUE, que deve corresponder ao nome da fila configurada no RabbitMQ do pacote config.
    public void listen(Message<OrderCreatedEventDto> message, Channel channel) {//Message é uma classe do Spring que encapsula a mensagem recebida, incluindo o payload e os headers. OrderCreatedEventDto é o tipo de dado que esperamos receber na mensagem, representando o evento de criação de pedido. Channel é usado para enviar confirmações (ack) ou rejeições (nack) da mensagem processada.
        logger.info("Mensagem recebida: {}", message);

        try {
            // Processa o payload
            OrderCreatedEventDto orderEvent = message.getPayload();
            orderService.save(orderEvent);//Chama o serviço para salvar o pedido na base de dados. O payload da mensagem contém os dados do pedido, que serão usados para criar um objeto OrderEntity e salvar no banco de dados, eh o conteudo da mensagem

            // Confirma manualmente que a mensagem foi processada
            channel.basicAck(
                    (Long) message.getHeaders().get("amqp_deliveryTag"), 
                    false
            );
        } catch (IOException ex) {
            try {
                // Envia NACK em caso de erro, evitando reentrega infinita, pois o padrão do RavvitMQ é reentregar mensagens que não foram confirmadas e caso esteja com uma falha, ficará dando erro infinitamente
                channel.basicNack(
                        (Long) message.getHeaders().get("amqp_deliveryTag"), 
                        false, 
                        false
                );
            } catch (IOException nackEx) {
                logger.error("Error while processing message: {}", message, nackEx);
            }

            logger.error("Erro ao processar mensagem: {}", message, ex);
            throw new OrderProcessingException("Error while processing order created event", ex);
        }
    }

}
