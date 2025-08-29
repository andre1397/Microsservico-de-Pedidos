package tech.desafiobtg.orderms.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.repository.MongoRepository;

import tech.desafiobtg.orderms.entity.OrderEntity;

//Repositorio do mongoDB, que é usado para acessar os dados do banco de dados. O primeiro parâmetro é o tipo da entidade que será manipulada, e o segundo é o tipo da chave primaria da entidade. No caso, a entidade OrderEntity e a chave primaria Long.
public interface OrderRepository extends MongoRepository<OrderEntity, Long>{

    Page<OrderEntity> findAllByCustomerId(Long customerId, PageRequest pageRequest);
    

}
