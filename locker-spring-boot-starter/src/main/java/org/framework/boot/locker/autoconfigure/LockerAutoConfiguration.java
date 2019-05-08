package org.framework.boot.locker.autoconfigure;

import org.framework.locker.aop.DistributedLockerPostProcessor;
import org.framework.locker.aop.aop.DistributedLockerAspect;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.integration.redis.util.RedisLockRegistry;

import java.util.List;
import java.util.Optional;

/**
 * lock auto configuration
 *
 * @author jiashuai.xie
 */
@EnableConfigurationProperties(LockerConfigurationProperties.class)
@Configuration
@AutoConfigureAfter(name = "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration")
@ConditionalOnClass(name = {
        "org.springframework.integration.redis.util.RedisLockRegistry",
        "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration"
})
public class LockerAutoConfiguration {

    @Autowired
    private ObjectProvider<List<DistributedLockerPostProcessor>> provider;

    @Autowired
    private LockerConfigurationProperties properties;


    @Bean
    @ConditionalOnMissingBean(name = "org.framework.locker.aop.aop.DistributedLockerAspect")
    public DistributedLockerAspect distributedLockAspect(RedisLockRegistry redisLockRegistry) {
        DistributedLockerAspect distributedLockAspect = new DistributedLockerAspect(redisLockRegistry, provider.getIfAvailable());
        Optional.ofNullable(properties.getOrder()).ifPresent(distributedLockAspect::setOrder);
        return distributedLockAspect;
    }

    @Bean
    @ConditionalOnMissingBean(name = "org.springframework.integration.redis.util.RedisLockRegistry")
    public RedisLockRegistry redisLockRegistry(RedisConnectionFactory redisConnectionFactory) {
        return new RedisLockRegistry(redisConnectionFactory, properties.getLockerKeyPrefix(), properties.getExpireAfter());
    }

}
