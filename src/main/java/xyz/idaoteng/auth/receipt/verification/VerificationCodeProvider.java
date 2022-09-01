package xyz.idaoteng.auth.receipt.verification;

public interface VerificationCodeProvider {
    VerificationCode next();

    boolean match(Object correctFeedback, Object feedback);
}
