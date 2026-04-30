FROM ubuntu:22.04 AS plugin-downloader

RUN apt-get update && apt-get install -y --no-install-recommends curl ca-certificates && \
    rm -rf /var/lib/apt/lists/*

ENV DEBEZIUM_VERSION=3.2.3.Final

RUN mkdir -p /plugins/debezium-mysql && \
    cd /plugins/debezium-mysql && \
    curl -fsSL -O https://repo1.maven.org/maven2/io/debezium/debezium-connector-mysql/${DEBEZIUM_VERSION}/debezium-connector-mysql-${DEBEZIUM_VERSION}-plugin.tar.gz && \
    tar -xzf debezium-connector-mysql-${DEBEZIUM_VERSION}-plugin.tar.gz --strip-components=1 && \
    rm debezium-connector-mysql-${DEBEZIUM_VERSION}-plugin.tar.gz

FROM apache/kafka:4.0.0

USER root

RUN mkdir -p /opt/kafka/logs /opt/kafka/plugins && \
    chown -R appuser:appuser /opt/kafka/logs /opt/kafka/plugins && \
    chmod -R 755 /opt/kafka/logs

RUN printf 'Configuration:\n  status: warn\n  Appenders:\n    Console:\n      name: STDOUT\n      target: SYSTEM_OUT\n      PatternLayout:\n        Pattern: "[%%d] %%p %%m (%%c)%%n"\n  Loggers:\n    Root:\n      level: INFO\n      AppenderRef:\n        ref: STDOUT\n' > /opt/kafka/config/connect-log4j2.yaml

COPY --from=plugin-downloader /plugins /opt/kafka/plugins
RUN chown -R appuser:appuser /opt/kafka/plugins

USER appuser

CMD ["/opt/kafka/bin/connect-distributed.sh", "/opt/kafka/config/connect-distributed.properties"]
