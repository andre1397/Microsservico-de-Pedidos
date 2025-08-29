package tech.desafiobtg.orderms.config;

import org.springframework.amqp.core.Declarable;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String ORDER_CREATED_QUEUE = "order-queue-created"; //Deve ser o mesmo nome da fila que foi criada no RabbitMQ no listener

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        // Cria um conversor de mensagens que converte objetos Java para JSON e vice-versa
        return new Jackson2JsonMessageConverter();//Não precisa de configuração adicional, pois o Spring Boot já configura o ObjectMapper automaticamente
    }

    @Bean
    public Declarable orderCreatedQueue() {// Cria a fila que será usada para receber mensagens de criação de pedidos
        // Declarable é uma interface que representa um objeto que pode ser declarado no RabbitMQ
        return new Queue(ORDER_CREATED_QUEUE);// Cria uma nova fila com o nome especificado na constante ORDER_CREATED_QUEUE. Ela é criada quando o aplicativo é iniciado, se ainda não existir, pra verificar se ela foi criada de fato, você pode acessar o RabbitMQ Management UI e verificar se a fila está lá, na aba Queues and Streams
    }
}
