package xyz.idaoteng.auth.receipt;

import lombok.extern.slf4j.Slf4j;
import xyz.idaoteng.auth.exceptions.NoReceiptException;
import xyz.idaoteng.auth.exceptions.OverfastRequestException;
import xyz.idaoteng.auth.receipt.verification.VerificationCode;
import xyz.idaoteng.auth.receipt.verification.VerificationCodeProvider;

@Slf4j
public abstract class BasicReceiptService implements ReceiptService{
    protected final VerificationCodeProvider codeProvider;

    //票据有效时间
    protected int receiptValidTime;

    //更新验证码的最短时间间隔（即更新验证码需要等待的最少时间）
    protected int verificationCodeInterval;

    //验证码有效时间
    protected int verificationCodeValidTime;

    public BasicReceiptService(ReceiptConfig config, VerificationCodeProvider codeProvider) {
        this.receiptValidTime = config.getReceiptValidTime();
        this.verificationCodeInterval = config.getVerificationCodeInterval();
        this.verificationCodeValidTime = config.getVerificationCodeInterval();
        this.codeProvider = codeProvider;
    }

    //生成存根
    protected Stub generateStub() {
        Stub stub = new Stub();
        stub.setReceiptId(nextReceiptId());
        stub.setReceiptDeadline(genReceiptDeadline());
        stub.setVerificationCodeCreateTime(genVerificationCodeCreateTime());
        stub.setVerificationCodeDeadline(genVerificationCodeDeadline());
        VerificationCode code = genVerificationCode();
        stub.setOutputObject(code.getOutputObject());
        stub.setCorrectFeedback(code.getCorrectFeedback());
        return stub;
    }

    //根据票据找出存根
    abstract protected Stub findStub(String receiptId);

    //根据存根生成票据
    protected Receipt generateReceipt(Stub stub) {
        Receipt receipt = new Receipt();
        receipt.setReceiptId(stub.getReceiptId());
        receipt.setOutputObject(stub.getOutputObject());
        return receipt;
    }

    //保存存根
    protected abstract void saveStub(Stub stub);

    //生成票据ID
    protected abstract String nextReceiptId();

    @Override
    public Receipt nextReceipt() {
        Stub stub = generateStub();
        Receipt receipt = generateReceipt(stub);
        saveStub(stub);
        return receipt;
    }

    @Override
    public Receipt refreshVerificationCode(String receiptId) throws OverfastRequestException, NoReceiptException {
        if (receiptId == null) {
            log.warn("更新验证码时：参数 receiptId 为 null");
            return null;
        }

        Stub stub = findStub(receiptId);
        if (stub == null) {
            throw new NoReceiptException("票据过期或其id不合法");
        }

        if (internalTooShort(stub.getVerificationCodeCreateTime())) {
            throw new OverfastRequestException("更新验证码的等待时间不足");
        }

        Receipt receipt = new Receipt();
        VerificationCode code = codeProvider.next();
        receipt.setReceiptId(receiptId);
        receipt.setOutputObject(code.getOutputObject());
        //保存新的验证码
        stub.setCorrectFeedback(code.getCorrectFeedback());
        stub.setVerificationCodeCreateTime(genVerificationCodeCreateTime());
        stub.setVerificationCodeDeadline(genVerificationCodeDeadline());
        saveStub(stub);
        return receipt;
    }

    @Override
    public boolean validateVerificationCode(String receiptId, Object feedback) throws NoReceiptException {
        if (receiptId == null) {
            log.warn("校验票据时：参数 receiptId 为 null");
            return false;
        }

        Stub stub = findStub(receiptId);
        if (stub == null) {
            throw new NoReceiptException("票据过期或其id不合法");
        }

        if (verificationCodeTimeOut(stub.getVerificationCodeDeadline())) {
            log.info("校验票据时: 验证码已过期");
            return false;
        }

        Object correctFeedback = stub.getCorrectFeedback();
        return codeProvider.match(correctFeedback, feedback);
    }

    protected Long genReceiptDeadline() {
        return System.currentTimeMillis() + (receiptValidTime * 60 * 1000L);
    }

    protected Long genVerificationCodeCreateTime() {
        return System.currentTimeMillis();
    }

    protected Long genVerificationCodeDeadline() {
        return System.currentTimeMillis() + (verificationCodeValidTime * 1000L);
    }

    protected VerificationCode genVerificationCode() {
        return codeProvider.next();
    }

    //检查验证码从生成到现在的时间间隔
    protected boolean internalTooShort(long createTime) {
        long diff = System.currentTimeMillis() - createTime;
        return diff < verificationCodeInterval;
    }

    //检查验证码是否失效
    protected boolean verificationCodeTimeOut(long deadline) {
        return System.currentTimeMillis() > deadline;
    }
}
