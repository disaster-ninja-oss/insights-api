package io.kontur.insightsapi.configuration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.List;

@Configuration
@EnableCaching
public class CacheConfig extends CachingConfigurerSupport {

    private final HashFunction hashFunction = Hashing.murmur3_32_fixed();

    @Bean("stringKeyGenerator")
    public KeyGenerator customStringKeyGenerator() {
        return (Object target, Method method, Object... params) -> {
            if (params.length == 1 && params[0] instanceof String) {
                return hashFunction.hashString((String) params[0], Charset.defaultCharset()).toString();
            }
            throw new IllegalArgumentException("Wrong params for StringKeyGenerator");
        };
    }

    @Bean("stringListKeyGenerator")
    public KeyGenerator customStringListKeyGenerator() {
        return (Object target, Method method, Object... params) -> {
            if (params.length == 2 && params[0] instanceof String && params[1] instanceof List) {
                return hashFunction.hashString((String) params[0], Charset.defaultCharset()) + "_"
                        + hashFunction.hashString(params[1].toString(), Charset.defaultCharset());
            }
            throw new IllegalArgumentException("Wrong params for StringListKeyGenerator");
        };
    }

    @Bean("stringStringKeyGenerator")
    public KeyGenerator customStringStringKeyGenerator() {
        return (Object target, Method method, Object... params) -> {
            if (params.length == 2 && params[0] instanceof String && params[1] instanceof String) {
                return hashFunction.hashString((String) params[0], Charset.defaultCharset()) + "_"
                        + hashFunction.hashString((String) params[1], Charset.defaultCharset());
            }
            throw new IllegalArgumentException("Wrong params for StringStringKeyGenerator");
        };
    }

    @Bean("listKeyGenerator")
    public KeyGenerator customListKeyGenerator() {
        return (Object target, Method method, Object... params) -> {
            if (params.length == 1 && params[0] instanceof List) {
                return hashFunction.hashString(params[0].toString(), Charset.defaultCharset());
            }
            throw new IllegalArgumentException("Wrong params for ListKeyGenerator");
        };
    }

    @Bean("stringStringListKeyGenerator")
    public KeyGenerator customStringStringListKeyGenerator() {
        return (Object target, Method method, Object... params) -> {
            if (params.length == 3 && params[0] instanceof String && params[1] instanceof String && params[2] instanceof List) {
                return hashFunction.hashString((String) params[0], Charset.defaultCharset()) + "_"
                        + hashFunction.hashString((String) params[1], Charset.defaultCharset()) + "_"
                        + hashFunction.hashString(params[2].toString(), Charset.defaultCharset());
            }
            throw new IllegalArgumentException("Wrong params for StringStringListKeyGenerator");
        };
    }

    @Bean
    public RedisCacheConfiguration cacheConfiguration(ObjectMapper objectMapper,
                                                      @Value("${spring.cache.redis.time-to-live}") Integer ttl) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(ttl))
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new CustomJsonRedisSerializer(objectMapper)));
    }

    @RequiredArgsConstructor
    private static class CustomJsonRedisSerializer implements RedisSerializer<Object> {

        private final ObjectMapper objectMapper;

        @Override
        public byte[] serialize(Object o) throws SerializationException {
            try {
                return objectMapper.writeValueAsBytes(o);
            } catch (JsonProcessingException e) {
                throw new SerializationException(e.getMessage(), e);
            }
        }

        @Override
        public Object deserialize(byte[] bytes) throws SerializationException {
            try {
                return objectMapper.readValue(bytes, Object.class);
            } catch (IOException e) {
                throw new SerializationException(e.getMessage(), e);
            }
        }
    }

}
