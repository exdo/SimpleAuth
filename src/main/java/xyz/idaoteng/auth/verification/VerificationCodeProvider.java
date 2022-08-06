package xyz.idaoteng.auth.verification;

public interface VerificationCodeProvider {
    VerificationCode next();

    boolean match(Object correctFeedback, Object feedback);
}
