package xyz.idaoteng.auth.receipt.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import xyz.idaoteng.auth.receipt.BasicReceiptService;
import xyz.idaoteng.auth.receipt.Stub;
import xyz.idaoteng.auth.verification.VerificationCodeProvider;
import xyz.idaoteng.auth.tools.RedisTemplateHolder;
import xyz.idaoteng.auth.tools.RedisUid;

import java.util.concurrent.TimeUnit;

@Component
@Conditional(NoOtherReceiptServiceCondition.class)
@Slf4j
public class RedisReceiptService extends BasicReceiptService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisUid redisUid;

    @Autowired
    public RedisReceiptService(RedisTemplateHolder holder,
                               RedisUid redisUid,
                               VerificationCodeProvider codeProvider) {
        super(codeProvider);
        this.redisTemplate = holder.get();
        this.redisUid = redisUid;
    }

    @Value("${receipt-validTime:10}")
    public void setReceiptValidTime(int validTime) {
        super.setReceiptValidTime(validTime);
    }

    @Value("${verificationCode-interval:30}")
    public void setVerificationCodeInterval(int interval) {
        super.setVerificationCodeInterval(interval);
    }

    @Value("${verificationCode-validTime:120}")
    public void setVerificationCodeValidTime(int validTime) {
        super.setVerificationCodeValidTime(validTime);
    }

    @Override
    protected String nextReceiptId() {
        return redisUid.nextId();
    }

    @Override
    protected void saveStub(Stub stub) {
        //验证码中的输出部分不必要存储
        stub.setOutputObject(null);
        redisTemplate.opsForValue().set(stub.getReceiptId(), stub);
        redisTemplate.expire(stub.getReceiptId(), receiptValidTime, TimeUnit.MINUTES);
    }

    @Override
    protected Stub findStub(String receiptId) {
        if (receiptId == null) return null;

        try {
            return (Stub) redisTemplate.opsForValue().get(receiptId);
        } catch (Exception e) {
            e.printStackTrace();
            log.warn("key对应的对象类型可能不是 Stub");
            return null;
        }
    }
}
