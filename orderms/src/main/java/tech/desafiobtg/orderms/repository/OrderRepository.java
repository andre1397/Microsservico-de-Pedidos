package tech.desafiobtg.orderms.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.repository.MongoRepository;

import tech.desafiobtg.orderms.entity.OrderEntity;

public interface OrderRepository extends MongoRepository<OrderEntity, Long>{

    Page<OrderEntity> findAllByCustomerId(Long customerId, PageRequest pageRequest);
    

}
