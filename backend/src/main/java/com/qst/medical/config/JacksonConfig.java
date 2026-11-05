package com.qst.medical.config;

import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Jackson 时间格式配置。
 *
 * <p>实体里的 LocalDateTime 默认会被序列化成 {@code 2021-11-18T17:34:02}（带 T、可能带毫秒），
 * 前端表格里会折行也不符合中文习惯，这里统一成 {@code yyyy-MM-dd HH:mm:ss}。</p>
 *
 * <p>实现方式用 {@link Jackson2ObjectMapperBuilderCustomizer} 而不是自己 new 一个 ObjectMapper：
 * Spring Boot 的 MappingJackson2HttpMessageConverter 用的是 builder 生成的 ObjectMapper，
 * 自己定义 ObjectMapper Bean 经常不生效（Boot 的 JacksonAutoConfiguration 已经注册过，
 * 加了 @ConditionalOnMissingBean 会被直接跳过）。</p>
 *
 * <p>null 字段不序列化由 application.properties 的
 * {@code spring.jackson.default-property-inclusion=non_null} 负责。</p>
 */
@Configuration
public class JacksonConfig {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer javaTimeCustomizer() {
        return builder -> {
            builder.serializerByType(LocalDateTime.class, new LocalDateTimeSerializer(DATE_TIME));
            builder.deserializerByType(LocalDateTime.class, new LocalDateTimeDeserializer(DATE_TIME));
            builder.serializerByType(LocalDate.class, new LocalDateSerializer(DATE));
            builder.deserializerByType(LocalDate.class, new LocalDateDeserializer(DATE));
        };
    }
}
