package xyz.idaoteng.auth.receipt;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@Getter
public class ReceiptConfig {
    //票据有效时间
    private int receiptValidTime = 10;

    //更新验证码的最短时间间隔（即更新验证码需要等待的最少时间）
    private int verificationCodeInterval = 30;

    //验证码有效时间
    private int verificationCodeValidTime = 120;

    //票据有效时间。默认10分钟
    @Value("${receipt-validTime:10}")
    public void setReceiptValidTime(int validTime) {
        if (validTime < 0 ) {
            throw new RuntimeException("票据有效时间不能小于等于0");
        } else if (validTime <= 2) {
            log.warn("票据有效时间小于等于2分钟");
        } else if (validTime > 30) {
            log.warn("票据有效时间高于30分钟");
        }
        log.info("票据有效时间 = {} 分钟", validTime);
        this.receiptValidTime = validTime;
    }

    //更新验证码需要等待的最少时间。默认30毫秒。单位毫秒
    @Value("${verificationCode-interval:30}")
    public void setVerificationCodeInterval(int interval) {
        if (interval <= 0 ) {
            throw new RuntimeException("更新验证码需要等待的最少时间不能小于等于0");
        } else if (interval > 60000) {
            log.warn("更新验证码需要等待的最少时间高于60000毫秒（60秒）");
        }
        log.info("更新验证码需要等待的最少时间 = {} 毫秒", interval);
        this.verificationCodeInterval = interval;
    }

    //验证码有效时间。默认120秒。单位秒。
    @Value("${verificationCode-validTime:120}")
    public void setVerificationCodeValidTime(int validTime) {
        if (validTime <= 0 ) {
            throw new RuntimeException("验证码有效时间不能小于等于0");
        } else if (validTime > (receiptValidTime * 60)) {
            throw new RuntimeException("验证码有效时间不能大于票据有效时间");
        } else if (validTime < 60) {
            log.warn("验证码有效时间低于60秒");
        } else if (validTime > 1800) {
            log.warn("验证码有效时间高于1800秒（30分钟）");
        }
        log.info("验证码有效时间 = {} 秒", validTime);
        this.verificationCodeValidTime = validTime;
    }
}
