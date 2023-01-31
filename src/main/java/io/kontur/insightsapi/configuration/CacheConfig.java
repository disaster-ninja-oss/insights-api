package io.kontur.insightsapi.configuration;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
@EnableCaching
public class CacheConfig extends CachingConfigurerSupport {

    private final HashFunction hashFunction = Hashing.murmur3_32_fixed();

    @Bean("stringKeyGenerator")
    public KeyGenerator customStringKeyGenerator() {
        return (Object target, Method method, Object... params) -> {
            if (params.length == 1 && params[0] instanceof String) {
                return hashFunction.hashString((String) params[0], StandardCharsets.UTF_8).toString();
            }
            throw new IllegalArgumentException("Wrong params for StringKeyGenerator");
        };
    }

    @Bean("stringListKeyGenerator")
    public KeyGenerator customStringListKeyGenerator() {
        return (Object target, Method method, Object... params) -> {
            if (params.length == 2 && params[0] instanceof String && params[1] instanceof List) {
                return hashFunction.hashString((String) params[0], StandardCharsets.UTF_8) + "_"
                        + hashFunction.hashString(params[1].toString(), StandardCharsets.UTF_8);
            }
            throw new IllegalArgumentException("Wrong params for StringListKeyGenerator");
        };
    }

    @Bean("stringStringKeyGenerator")
    public KeyGenerator customStringStringKeyGenerator() {
        return (Object target, Method method, Object... params) -> {
            if (params.length == 2 && params[0] instanceof String && params[1] instanceof String) {
                return hashFunction.hashString((String) params[0], StandardCharsets.UTF_8) + "_"
                        + hashFunction.hashString((String) params[1], StandardCharsets.UTF_8);
            }
            throw new IllegalArgumentException("Wrong params for StringStringKeyGenerator");
        };
    }

    @Bean("listKeyGenerator")
    public KeyGenerator customListKeyGenerator() {
        return (Object target, Method method, Object... params) -> {
            if (params.length == 1 && params[0] instanceof List) {
                return hashFunction.hashString(params[0].toString(), StandardCharsets.UTF_8);
            }
            throw new IllegalArgumentException("Wrong params for ListKeyGenerator");
        };
    }

    @Bean("threeParametersAsStringOrListKeyGenerator")
    public KeyGenerator customThreeParametersAsStringOrListKeyGenerator() {
        return (Object target, Method method, Object... params) -> {
            if (params.length == 3 && (params[0] instanceof String || params[0] instanceof List)
                    && (params[1] instanceof String || params[1] instanceof List)
                    && (params[2] instanceof String || params[2] instanceof List)) {
                return hashFunction.hashString(params[0].toString(), StandardCharsets.UTF_8) + "_"
                        + hashFunction.hashString(params[1].toString(), StandardCharsets.UTF_8) + "_"
                        + hashFunction.hashString(params[2].toString(), StandardCharsets.UTF_8);
            }
            throw new IllegalArgumentException("Wrong params for StringStringListKeyGenerator");
        };
    }
}
