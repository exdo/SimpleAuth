package xyz.idaoteng.auth.receipt;

import lombok.Data;

@Data
public class Reply {
    private String receiptId;
    private Object feedback;
}
