package xyz.idaoteng.auth.receipt;

import lombok.Data;

@Data
public class Stub {
    private String receiptId;
    private Long receiptDeadline;
    private Long verificationCodeCreateTime;
    private Long verificationCodeDeadline;
    private Object correctFeedback;
    private Object outputObject;
}
