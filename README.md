kafka-outbox 루트에서 인프라를 올립니다.
docker compose up -d

source DB용 Debezium connector를 등록합니다.
curl -X POST http://localhost:8083/connectors -H "Content-Type: application/json" -d @debezium/user-outbox-connector.json

loginservice를 실행합니다.
이 서비스가 회원가입 요청을 받아 source DB에 users와 outbox_event를 씁니다.

회원가입 요청을 보냅니다.
POST http://localhost:8081/register
body: {"username":"alice","password":"password"}

전파 결과를 확인합니다.
3308의 outbox_event가 늘고, 3307의 customer에 새 row가 들어오면 정상입니다.



# Kafka Outbox CDC

A minimal Spring Boot + Kafka + Debezium structure for user signup event propagation with the outbox pattern.

## Flow

1. `user-service` creates a user and an outbox row in the same database transaction.
2. Debezium watches the outbox table, applies the outbox SMT, and publishes domain events to Kafka.
3. `notification-service` consumes the Kafka event and handles downstream propagation.

## Important detail

Debezium should not read the `users` table directly for this flow. The `users` table stores business state, and the `outbox_event` table stores the message to be published. The connector reads only the outbox table and routes the payload into Kafka as a domain event.

For local development, Kafka is exposed on `localhost:29092`.

This project uses KRaft mode, so ZooKeeper is not required.

## Modules

- `shared-events`: shared event contract
- `user-service`: signup API + outbox writer
- `notification-service`: Kafka consumer for downstream propagation

## Key idea

The application never publishes directly to Kafka inside the signup transaction. It only writes business data and an outbox record. CDC handles delivery from the database to Kafka.

## Next steps

- Create the MariaDB schema
- Start Kafka, MariaDB, and Debezium with Docker Compose
- Register the Debezium outbox connector
- Run `user-service` and `notification-service`

## Register connector

After `docker compose up -d`:

```bash
curl -X POST http://localhost:8083/connectors \
	-H 'Content-Type: application/json' \
	--data @debezium/user-outbox-connector.json
```

## Run locally

1. Start infrastructure

```bash
docker compose up -d
```

2. Register the Debezium connector

```bash
curl -X POST http://localhost:8083/connectors \
	-H 'Content-Type: application/json' \
	--data @debezium/user-outbox-connector.json
```

3. Run `user-service`

```bash
cd user-service
gradle bootRun
```

4. Run `notification-service`

```bash
cd notification-service
gradle bootRun
```

5. Call the signup API

```bash
curl -X POST http://localhost:8081/users \
	-H 'Content-Type: application/json' \
	-d '{"email":"test@example.com","name":"Test User","password":"password123"}'
```

If Gradle is not installed on your machine, install it first or add a Gradle wrapper to the repository.
# backend-kafka-outbox
