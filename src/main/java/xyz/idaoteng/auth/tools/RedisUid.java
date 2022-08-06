package xyz.idaoteng.auth.tools;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Component
public class RedisUid {
    private final RedisAtomicLong atomicLong;

    @Autowired
    public RedisUid(RedisTemplateHolder holder) {
        atomicLong = new RedisAtomicLong("REDIS_COUNTER",
                Objects.requireNonNull(holder.get().getConnectionFactory()));
        atomicLong.set(0);
    }

    private void checkHour() {
        if (LocalDateTime.now().getHour() == 0) {
            atomicLong.set(0);
        }
    }

    private String getDefaultTimeString() {
        checkHour();
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    public String nextId() {
        String prefix = getDefaultTimeString();
        long suffix = atomicLong.incrementAndGet();
        return String.format("%1$s%2$05d", prefix, suffix);
    }
}
