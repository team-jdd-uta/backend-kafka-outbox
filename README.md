# backend-kafka-outbox

CDC outbox 패턴을 검증하기 위한 Kafka, Debezium, MariaDB, Conduktor 예제 레포입니다. 현재 Kubernetes 배포에서 직접 사용하는 주요 산출물은 Debezium Kafka Connect 커스텀 이미지 빌드 컨텍스트인 `kafka-connect/Dockerfile`입니다.

이 레포의 `user-service`와 `notification-service`는 outbox 패턴 참고 구현입니다. 실제 서비스 배포 경로에서는 `backend-login-service`가 source DB/outbox를 쓰고, `backend-user-service`가 Kafka 이벤트를 소비해 `customer` 테이블에 projection합니다.

## 역할

- 로컬 Docker Compose로 Kafka, MariaDB, Debezium, Conduktor를 실험합니다.
- Debezium Outbox EventRouter 설정 예제를 제공합니다.
- Kubernetes/Harbor 배포용 Kafka Connect 이미지를 빌드합니다.
- outbox 패턴 참고 구현(`user-service`, `notification-service`, `shared-events`)을 보관합니다.

## 실제 MSA 배포에서의 위치

```text
backend-login-service
  -> login-mariadb.app.users
  -> login-mariadb.app.outbox_event
  -> Debezium Kafka Connect
  -> Kafka topic outbox.event.user
  -> backend-user-service Kafka consumer
  -> user-mariadb.app_target.customer
```

## 주요 디렉터리

| Path | 설명 |
| --- | --- |
| `kafka-connect/Dockerfile` | Debezium MySQL connector plugin을 포함한 Kafka Connect 이미지 |
| `debezium/user-outbox-connector.json` | 로컬 Compose용 Debezium connector 예제 |
| `docker-compose.yml` | 로컬 Kafka/MariaDB/Debezium/Conduktor/notification-service 구성 |
| `init-source.sql` | source MariaDB Debezium 권한 grant |
| `user-service/` | 참고용 signup + outbox writer |
| `notification-service/` | 참고용 Kafka consumer |
| `shared-events/` | 참고용 event contract |
| `kafka_debezium/` | 기존 실험 복사본 |

## Kafka Connect 이미지

이미지 빌드:

```bash
docker build -t team9-debezium-connect:local kafka-connect
```

이미지 특징:

- base image: `apache/kafka:4.0.0`
- Debezium MySQL connector: `3.2.3.Final`
- plugin path: `/opt/kafka/plugins`
- entrypoint: `connect-distributed.sh`

Kubernetes에서는 `k8s/debezium-connect-configmap.yaml`의 `connect-distributed.properties`를 mount해서 실행합니다.

## 로컬 Compose 실행
curl -X POST http://localhost:8083/connectors \
	-H 'Content-Type: application/json' \
	--data @debezium/user-outbox-connector.json
```

For sharded source DBs, register one connector per shard:

```bash
curl -X POST http://localhost:8083/connectors \
	-H 'Content-Type: application/json' \
	--data @debezium/user-outbox-connector-shard-01.json

curl -X POST http://localhost:8083/connectors \
	-H 'Content-Type: application/json' \
	--data @debezium/user-outbox-connector-shard-02.json
```

`user-outbox-connector-shard-02.json` connects to the `mariadb_source_shard_02` service added in `docker-compose.yml`.

## Run locally

1. Start infrastructure

```bash
docker compose up -d
```

Conduktor Console:

```text
http://localhost:8080
```

Kafka bootstrap:

```text
localhost:29092
```

Debezium Connect REST:

```text
http://localhost:8083
```

## 로컬 connector 등록

```bash
curl -X POST http://localhost:8083/connectors \
  -H "Content-Type: application/json" \
  --data @debezium/user-outbox-connector.json
```

현재 `debezium/user-outbox-connector.json`은 로컬 Compose용입니다.

- connector name: `user-outbox-connector-v2`
- database hostname: `mariadb`
- topic routing: `outbox.event.${routedByValue}`
- outbox table: `app.outbox_event`

Kubernetes에서는 `k8s/login-outbox-connector-job.yaml`의 inline JSON을 사용하며 connector name은 `login-outbox-connector`입니다.

## 참고 구현 실행

```bash
cd user-service
gradle bootRun
```

```bash
cd notification-service
gradle bootRun
```

회원가입 요청 예:

```bash
curl -X POST http://localhost:8081/users \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","name":"Test User","password":"password123"}'
```

## 주의점

- 이 레포의 `notification-service`는 실제 MSA target consumer가 아닙니다.
- 실제 target projection은 `backend-user-service`가 담당합니다.
- `debezium/user-outbox-connector.json`은 로컬용이고, Kubernetes connector 설정과 이름/host가 다릅니다.
- 현재 레포에는 과거 빌드 산출물(`build/`, `.gradle/`, `.class`)이 일부 포함되어 있습니다. 별도 cleanup issue로 제거하는 것이 좋습니다.
