package com.example.kafka.userservice.mapper;

import com.example.kafka.userservice.domain.OutboxEvent;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OutboxEventMapper {

    @Insert("""
            INSERT INTO outbox_event (
                event_id,
                aggregate_type,
                aggregate_id,
                event_type,
                payload,
                status,
                created_at,
                published_at
            ) VALUES (
                #{eventId},
                #{aggregateType},
                #{aggregateId},
                #{eventType},
                #{payload},
                #{status},
                #{createdAt},
                #{publishedAt}
            )
            """)
    int insert(OutboxEvent event);

    @Select("""
            SELECT id,
                   event_id AS eventId,
                   aggregate_type AS aggregateType,
                   aggregate_id AS aggregateId,
                   event_type AS eventType,
                   payload,
                   status,
                   created_at AS createdAt,
                   published_at AS publishedAt
            FROM outbox_event
            WHERE event_id = #{eventId}
            LIMIT 1
            """)
    OutboxEvent findByEventId(@Param("eventId") String eventId);
}
