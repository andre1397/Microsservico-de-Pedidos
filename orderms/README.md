# OrderMS - Microserviço de Pedidos

Este projeto implementa um microserviço de **gerenciamento de pedidos** utilizando:
- **Spring Boot**
- **MongoDB** como banco de dados
- **RabbitMQ** para mensageria (eventos de criação de pedidos)

---

## Tecnologias Utilizadas
- Java 21
- Spring Boot
- Spring Data MongoDB
- RabbitMQ
- Docker e Docker Compose
- Maven

---

## Estrutura do Projeto
- `OrderController` → Endpoints REST para listar e detalhar pedidos
- `OrderService` → Regras de negócio e persistência
- `OrderEntity` e `OrderItem` → Entidades persistidas no MongoDB
- `OrderCreatedEventDto` → DTO recebido via RabbitMQ
- `RabbitMqConfig` → Configuração de filas e conversores de mensagens

---

## Pré-requisitos
- [Docker](https://docs.docker.com/get-docker/)
- [Docker Compose](https://docs.docker.com/compose/)
- [Maven](https://maven.apache.org/) (opcional, caso queira rodar localmente sem Docker)

---

## Como Executar

### 1. Subir dependências com Docker
O projeto utiliza **MongoDB** e **RabbitMQ**.  
Suba os serviços executando:

docker compose up -d

Isso irá iniciar:

MongoDB na porta 27017

Usuário: admin | Senha: 123

## RabbitMQ

Porta 5672: comunicação da aplicação

Porta 15672: painel web → http://localhost:15672
Usuário: guest | Senha: guest

### 2. Configuração do Spring Boot
O projeto já está configurado em application.properties para conectar no MongoDB local:

### properties:
```
spring.data.mongodb.host=localhost
spring.data.mongodb.port=27017
spring.data.mongodb.database=desafiobtgdb
spring.data.mongodb.username=admin
spring.data.mongodb.password=123
```

### 3. Rodando a Aplicação
Com as dependências ativas, execute:
```
./mvnw spring-boot:run
```
ou, se preferir:
```
mvn spring-boot:run
```
A aplicação subirá na porta 8080.

### Endpoints da API
#### ➤ Listar pedidos de um cliente

### Com paginação:
```
GET /customers/{customerId}/orders?page={page}&pageSize={pageSize}
```
Exemplo:
```
curl "http://localhost:8080/customers/1/orders?page=0&pageSize=10"
```
#### Resposta:

```
{
  "summary": {
    "totalOnOrders": 199.98
  },
  "data": [
    {
      "orderId": 101,
      "customerId": 1,
      "totalValue": 99.99
    }
  ],
  "pagination": {
    "page": 0,
    "pageSize": 10,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

### Sem paginação (fica com os seguintes valores padrão: page=0 e pageSize=10):
```
GET /orders?customerId=1
```

Exemplo de resposta:
```
[
  {
    "id": "66d0d41e27a8c93b5c123456",
    "orderCode": 12345,
    "customerId": 1,
    "totalValue": 3740.00
  },
  {
    "id": "66d0d41e27a8c93b5c654321",
    "orderCode": 12346,
    "customerId": 1,
    "totalValue": 200.00
  }
]
```

### Detalhar um pedido por ID
```
GET /orders/{orderId}
```
Exemplo:
```
curl "http://localhost:8080/orders/101"
```
Resposta:
```
{
  "orderId": 101,
  "customerId": 1,
  "totalValue": 99.99,
  "items": [
    {
      "product": "Notebook",
      "quantity": 1,
      "price": 99.99
    }
  ]
}
```
### Integração com RabbitMQ
O serviço consome eventos de criação de pedidos a partir da fila:

order-queue-created
Formato do JSON esperado:
```
{
  "orderCode": 101,
  "customerCode": 1,
  "items": [
    {
      "product": "Notebook",
      "quantity": 1,
      "price": 99.99
    }
  ]
}
```
Ao receber a mensagem, o pedido é persistido no MongoDB automaticamente.

### Desenvolvimento e Testes
Para rodar os testes unitários:
```
mvn test
```
## Observações
O MongoDB cria automaticamente o banco definido em spring.data.mongodb.database.

Índices são criados automaticamente (spring.data.mongodb.auto-index-creation=true).

Caso queira acessar o RabbitMQ, use: http://localhost:15672.

## Conclusão
Este microserviço faz parte de um sistema distribuído e é responsável por:

- Receber e persistir pedidos a partir de mensagens do RabbitMQ;
- Consultar pedidos via API REST;
- Calcular totais por cliente;
