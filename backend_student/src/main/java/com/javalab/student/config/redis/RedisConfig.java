package com.javalab.student.config.redis;

import com.javalab.student.service.MessageSubscriberService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching // Spring의 캐싱 기능 활성화
public class RedisConfig {

    /**
     * 🔹 application.yml에서 Redis 설정 값 가져오기
     */
    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Value("${spring.data.redis.password}") // ✅ 비밀번호 추가
    private String redisPassword;

    /**
     * 🔹 Redis 연결 팩토리 (비밀번호 설정 추가)
     */
    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisHost);
        config.setPort(redisPort);
        config.setPassword(RedisPassword.of(redisPassword)); // ✅ 비밀번호 설정 추가

        return new LettuceConnectionFactory(config);
    }

    /**
     * 🔹 사용자 권한 관리용 RedisTemplate (Object 저장)
     */
    @Bean
    @Primary // 기본적으로 주입되는 RedisTemplate 지정
    public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Key 직렬화 방식 설정 (문자열)
        template.setKeySerializer(new StringRedisSerializer());

        // Value 직렬화 방식 설정 (JSON 변환)
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        return template;
    }

    /**
     * 🔹 CacheManager 빈 등록
     */
    @Bean
    public CacheManager cacheManager(LettuceConnectionFactory redisConnectionFactory) {
        return RedisCacheManager.builder(redisConnectionFactory).build();
    }

    /**
     * 🔹 메시징 전용 RedisTemplate (String 저장)
     */
    @Bean(name = "redisStringTemplate")
    public RedisTemplate<String, String> redisStringTemplate(LettuceConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(stringSerializer);

        return template;
    }

    /**
     * 🔹 Redis Pub/Sub 메시지 리스너 컨테이너 설정
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            LettuceConnectionFactory connectionFactory, MessageSubscriberService subscriber) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(new MessageListenerAdapter(subscriber), new PatternTopic("chat_channel"));
        return container;
    }
}
