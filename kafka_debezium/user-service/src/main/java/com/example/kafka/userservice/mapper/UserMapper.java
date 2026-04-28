package com.example.kafka.userservice.mapper;

import com.example.kafka.userservice.domain.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {

    @Insert("""
            INSERT INTO users (user_id, email, name, password_hash, created_at)
            VALUES (#{userId}, #{email}, #{name}, #{passwordHash}, #{createdAt})
            """)
    int insert(User user);

    @Select("""
            SELECT user_id AS userId,
                   email,
                   name,
                   password_hash AS passwordHash,
                   created_at AS createdAt
            FROM users
            WHERE email = #{email}
            LIMIT 1
            """)
    User findByEmail(@Param("email") String email);
}
