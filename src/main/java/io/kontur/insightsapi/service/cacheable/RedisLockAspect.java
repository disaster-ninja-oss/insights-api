package io.kontur.insightsapi.service.cacheable;

import io.kontur.insightsapi.service.cacheable.RedisLock;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Aspect
@Component
@Order(1)  // run before @Cacheable
public class RedisLockAspect {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ApplicationContext applicationContext;

    @Around("@annotation(redisLock)")
    public Object around(ProceedingJoinPoint joinPoint, RedisLock redisLock) throws Throwable {
        // simple lock algo as suggested in https://redis.io/docs/latest/develop/use/patterns/distributed-locks/
        String key = generateKey(joinPoint, redisLock);
        String lockValue = Thread.currentThread().getId() + "_" + System.nanoTime();
        boolean isLocked = acquireLock(key, lockValue, redisLock);
        if (!isLocked) {
            throw new RuntimeException("Unable to acquire lock: job still in progress");
        }
        try {
            return joinPoint.proceed();
        } finally {
            releaseLock(key, lockValue);
        }
    }

    private String generateKey(ProceedingJoinPoint joinPoint, RedisLock redisLock) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Cacheable cacheableAnnotation = signature.getMethod().getAnnotation(Cacheable.class);
        // construct the key as @Cacheable would do
        String key = cacheableAnnotation.key();
        if (key.isEmpty()) {
            String keyGeneratorName = cacheableAnnotation.keyGenerator();
            KeyGenerator keyGenerator = applicationContext.getBean(keyGeneratorName, KeyGenerator.class);
            key = (String) keyGenerator.generate(joinPoint.getTarget(), signature.getMethod(), joinPoint.getArgs());
        }
        return "lock::" + cacheableAnnotation.value()[0] + "::" + key;
    }

    private boolean acquireLock(String key, String lockValue, RedisLock redisLock) {
        // DN-BE won't wait longer than 1 minute and will return 504 to the client if still no response.
        // So let's wait for a little longer and possibly fail. One of the next retries will catch up
        long waitTimeout = TimeUnit.SECONDS.toMillis(80);
        long elapsedTime = 0;
        while (elapsedTime < waitTimeout) {
            Boolean success = redisTemplate.opsForValue().setIfAbsent(key, lockValue, redisLock.timeout(), redisLock.timeUnit());
            if (success != null && success) {
                return true;
            }
            try {
                Thread.sleep(1000);  // each second try to acquire the lock
            } catch (InterruptedException e) {
                releaseLock(key, lockValue);
                Thread.currentThread().interrupt();  // Restore the interrupted status
                return false;
            }
            elapsedTime += 1000;
        }
        return false;
    }

    private void releaseLock(String key, String lockValue) {
        // remove the key only if it's set by the current thread
        if (lockValue.equals(redisTemplate.opsForValue().get(key))) {
            redisTemplate.delete(key);
        }
    }
}
