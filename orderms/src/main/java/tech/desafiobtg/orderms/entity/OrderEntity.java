package tech.desafiobtg.orderms.entity;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Document(collection = "tb_orders") //collection no mongoDB é o equivalente as tabelas do banco relacional. Document eh a classe que representa a tabela, como eh a entity no relacional
public class OrderEntity {

    @MongoId //MongoId eh o equivalente ao @Id do JPA, que indica que o campo eh a chave primaria
    private Long orderId;

    @Indexed(name = "customer_id_index") //Indexed eh usado para criar um indice no campo, o que melhora a performance das consultas
    private Long customerId;

    @Field(targetType=FieldType.DECIMAL128) //Field eh usado para mapear o campo no MongoDB, targetType eh usado para especificar o tipo do campo no MongoDB, eh necessario pois o mongo salva o bigDecimal como texto, então usando essa annotation é possivel alterar o tipo de dado que sera inserido no BD, nesse caso DECIMAL128 que é um tipo de numero decimal
    private BigDecimal totalValue;

    private List<OrderItem> items;

    public OrderEntity() {
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public BigDecimal getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(BigDecimal totalValue) {
        this.totalValue = totalValue;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    
}
