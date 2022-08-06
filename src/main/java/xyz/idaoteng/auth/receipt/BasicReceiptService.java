package xyz.idaoteng.auth.receipt;

import lombok.extern.slf4j.Slf4j;
import xyz.idaoteng.auth.exceptions.NoReceiptException;
import xyz.idaoteng.auth.exceptions.OverfastRequestException;
import xyz.idaoteng.auth.verification.VerificationCode;
import xyz.idaoteng.auth.verification.VerificationCodeProvider;

@Slf4j
public abstract class BasicReceiptService implements ReceiptService{
    protected final VerificationCodeProvider codeProvider;

    //票据有效时间
    protected int receiptValidTime = 10;

    //更新验证码的最短时间间隔（即更新验证码需要等待的最少时间）
    protected int verificationCodeInterval = 30;

    //验证码有效时间
    protected int verificationCodeValidTime = 120;

    public BasicReceiptService(VerificationCodeProvider codeProvider) {
        this.codeProvider = codeProvider;
    }

    //票据有效时间。默认10分钟
    protected void setReceiptValidTime(int validTime) {
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
    protected void setVerificationCodeInterval(int interval) {
        if (interval <= 0 ) {
            throw new RuntimeException("更新验证码需要等待的最少时间不能小于等于0");
        } else if (interval > 60000) {
            log.warn("更新验证码需要等待的最少时间高于60000毫秒（60秒）");
        }
        log.info("更新验证码需要等待的最少时间 = {} 毫秒", interval);
        this.verificationCodeInterval = interval;
    }

    //验证码有效时间。默认120秒。单位秒。
    protected void setVerificationCodeValidTime(int validTime) {
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
