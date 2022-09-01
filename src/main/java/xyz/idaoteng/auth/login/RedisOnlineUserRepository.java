package xyz.idaoteng.auth.login;

import io.lettuce.core.RedisCommandExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import xyz.idaoteng.auth.login.BasicRepository;
import xyz.idaoteng.auth.login.NoOtherRepositoryCondition;
import xyz.idaoteng.auth.subject.UserInfo;
import xyz.idaoteng.auth.tools.RedisTemplateHolder;
import xyz.idaoteng.auth.utils.NetUtil;
import xyz.idaoteng.auth.utils.PasswordUtil;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@Conditional(NoOtherRepositoryCondition.class)
@Slf4j
public class RedisOnlineUserRepository extends BasicRepository {
    private final String suffix;

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisOnlineUserRepository(@Value("${max-online-time:0}") int maxOnlineTime,
                                     RedisTemplateHolder holder) {
        super(maxOnlineTime);
        this.redisTemplate = holder.get();

        byte[] macAddress = NetUtil.getMacAddress();
        if (macAddress == null) {
            log.info("没有获取到本地Mac地址");
            suffix = PasswordUtil.randomString(15);
        } else {
            suffix = Hex.encodeHexString(macAddress);
        }
    }

    @Override
    public String add(UserInfo userInfo) {
        String redisUserId = UUID.randomUUID() + "@" + suffix;
        redisTemplate.opsForValue().set(redisUserId, userInfo);
        redisTemplate.expire(redisUserId, maxOnlineTime, TimeUnit.MINUTES);

        return redisUserId;
    }

    @Override
    public UserInfo get(String authToken) {
        try {
            return (UserInfo) redisTemplate.opsForValue().get(authToken);
        } catch (RedisCommandExecutionException e) {//redisUserId指向的对象并非UserInfo类型
            return null;
        }
    }

    @Override
    public void remove(String authToken) {
        redisTemplate.delete(authToken);
    }
}
