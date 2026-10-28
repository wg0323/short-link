package com.shortlink.redirect.config;

import com.shortlink.common.constant.RedisKeys;
import com.shortlink.redirect.bloom.BloomFilterService;
import com.shortlink.redirect.cache.LinkCacheService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.nio.charset.StandardCharsets;

/**
 * 订阅两类广播：
 * <ul>
 *   <li>{@code sl:link:add} —— 新短链上线，加入布隆过滤器；</li>
 *   <li>{@code sl:link:evict} —— 短链修改或删除，清掉本地缓存。</li>
 * </ul>
 */
@Configuration
public class RedisSubscriberConfig {

    @Bean
    public RedisMessageListenerContainer linkEventContainer(RedisConnectionFactory connectionFactory,
                                                            LinkCacheService cacheService,
                                                            BloomFilterService bloomFilterService) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        container.addMessageListener((message, pattern) ->
                        bloomFilterService.put(new String(message.getBody(), StandardCharsets.UTF_8)),
                new ChannelTopic(RedisKeys.LINK_ADD_CHANNEL));

        container.addMessageListener((message, pattern) ->
                        cacheService.invalidate(new String(message.getBody(), StandardCharsets.UTF_8)),
                new ChannelTopic(RedisKeys.LINK_EVICT_CHANNEL));

        return container;
    }
}
