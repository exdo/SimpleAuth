package xyz.idaoteng.auth.verification;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class VerificationCode {
    private Object correctFeedback;
    private Object outputObject;
}
