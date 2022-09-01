package xyz.idaoteng.auth.receipt.verification;

import com.wf.captcha.SpecCaptcha;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

import static com.wf.captcha.base.Captcha.FONT_5;

@Component
@Conditional(NoOtherProviderCondition.class)
@Slf4j
public class EasyCaptcha implements VerificationCodeProvider {
    private final LinkedBlockingQueue<VerificationCode> captchaQueue = new LinkedBlockingQueue<>(500);

    private class CaptchaProducer implements Runnable {
        @Override
        public void run() {
            while (true) {
                SpecCaptcha captcha = new SpecCaptcha(100, 40, 4);
                try {
                    captcha.setFont(FONT_5);
                } catch (IOException | FontFormatException e) {
                    throw new RuntimeException(e);
                }
                VerificationCode code = new VerificationCode();
                String text = captcha.text();
                String imgString = captcha.toBase64();
                code.setCorrectFeedback(text);
                code.setOutputObject(imgString);
                try {
                    captchaQueue.put(code);
                    log.trace("验证码 +1");
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public EasyCaptcha() {
        new Thread(new CaptchaProducer(), "captchaProducer-thread").start();
    }

    @Override
    public VerificationCode next() {
        VerificationCode code;
        try {
            code = captchaQueue.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        log.debug("验证码的正确反馈 = {}", code.getCorrectFeedback());
        return code;
    }

    @Override
    public boolean match(Object correctFeedback, Object feedback) {
        if (correctFeedback == null) {
            log.info("校验验证码时：参数 correctFeedback 为 null");
            return false;
        }

        String answer = (String) correctFeedback;
        String uploadedAnswer;
        try {
            uploadedAnswer = (String) feedback;
        } catch (Exception e) {
            log.info("验证码反馈类型错误：参数 feedback 无法转换为 String 类型");
            return false;
        }
        return answer.equalsIgnoreCase(uploadedAnswer);
    }
}
