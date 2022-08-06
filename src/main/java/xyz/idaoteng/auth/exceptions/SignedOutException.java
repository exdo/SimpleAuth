package xyz.idaoteng.auth.exceptions;

public class SignedOutException extends RuntimeException{
    public SignedOutException() {
        super("已登出");
    }
}
