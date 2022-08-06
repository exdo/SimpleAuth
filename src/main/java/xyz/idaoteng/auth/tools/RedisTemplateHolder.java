package xyz.idaoteng.auth.tools;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;
import xyz.idaoteng.auth.utils.JacksonUtil;

@Component
public class RedisTemplateHolder {
    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public RedisTemplateHolder(LettuceConnectionFactory lettuceConnectionFactory) {
        this.redisTemplate = createRedisTemplate(lettuceConnectionFactory);
    }

    //配置 <String, Object> 类型的 RedisTemplate<>
    private RedisTemplate<String, Object> createRedisTemplate(LettuceConnectionFactory lettuceConnectionFactory) {
        //创建依赖对象
        ObjectMapper objectMapper = JacksonUtil.objectMapper();
        //为Redis配置Jackson映射器和映射规则
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper om = objectMapper.copy();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.activateDefaultTyping(om.getPolymorphicTypeValidator(), ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        jackson2JsonRedisSerializer.setObjectMapper(om);
        //配置Redis的映射方式
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(jackson2JsonRedisSerializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(jackson2JsonRedisSerializer);
        //配置Redis连接池
        template.setConnectionFactory(lettuceConnectionFactory);
        template.afterPropertiesSet();
        return template;
    }

    public RedisTemplate<String, Object> get() {
        return this.redisTemplate;
    }
}
