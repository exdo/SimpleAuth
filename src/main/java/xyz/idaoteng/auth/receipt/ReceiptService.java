package xyz.idaoteng.auth.receipt;

import xyz.idaoteng.auth.exceptions.NoReceiptException;
import xyz.idaoteng.auth.exceptions.OverfastRequestException;

public interface ReceiptService {
    //生成下一个票据
    Receipt nextReceipt();

    //更新验证码
    Receipt refreshVerificationCode(String receiptId) throws OverfastRequestException, NoReceiptException;

    //校验票据
    boolean validateVerificationCode(String receiptId, Object feedback) throws NoReceiptException;
}
