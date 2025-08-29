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

@Component 


public class OrderCreatedListener {

    private final Logger logger = LoggerFactory.getLogger(OrderCreatedListener.class); 

    private final OrderService orderService;

    public OrderCreatedListener(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Listener para a fila de pedidos criados.
     * Usa Message<OrderCreatedEventDto> para capturar o payload e headers da mensagem.
     * ackMode="MANUAL" permite confirmar manualmente o recebimento.
     */
    @RabbitListener(queues = ORDER_CREATED_QUEUE, ackMode = "MANUAL")
    public void listen(Message<OrderCreatedEventDto> message, Channel channel) {
        logger.info("Mensagem recebida: {}", message);

        try {
            
            OrderCreatedEventDto orderEvent = message.getPayload();
            orderService.save(orderEvent);

            
            channel.basicAck(
                    (Long) message.getHeaders().get("amqp_deliveryTag"), 
                    false
            );
        } catch (IOException ex) {
            try {
                
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
