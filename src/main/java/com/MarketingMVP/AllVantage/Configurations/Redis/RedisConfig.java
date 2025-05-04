package com.MarketingMVP.AllVantage.Configurations.Redis;

import com.MarketingMVP.AllVantage.DTOs.Facebook.AccountToken.FacebookAccountTokenDTO;
import com.MarketingMVP.AllVantage.DTOs.Facebook.PageToken.FacebookPageTokenDTO;
import com.MarketingMVP.AllVantage.DTOs.LinkedIn.AccountToken.LinkedinTokenDTO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, FacebookAccountTokenDTO> redisAccountTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, FacebookAccountTokenDTO> template = new RedisTemplate<>();

        template.setConnectionFactory(redisConnectionFactory);
        // Use JSON serialization for values
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisTemplate<String, FacebookPageTokenDTO> redisPageTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, FacebookPageTokenDTO> template = new RedisTemplate<>();

        template.setConnectionFactory(redisConnectionFactory);
        // Use JSON serialization for values
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisTemplate<String, LinkedinTokenDTO> redisLinkedinTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, LinkedinTokenDTO> template = new RedisTemplate<>();

        template.setConnectionFactory(redisConnectionFactory);
        // Use JSON serialization for values
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }
}
